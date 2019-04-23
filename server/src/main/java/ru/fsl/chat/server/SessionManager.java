package ru.fsl.chat.server;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.fsl.chat.server.authorization.*;
import ru.fsl.transport.tcp.TcpSession;
import ru.fsl.transport.tcp.TcpSessionImpl;
import ru.fsl.transport.tcp.TcpSessionNotFoundException;
import ru.fsl.transport.tcp.TcpSessionsManager;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager implements TcpSessionsManager, UserSessionsManager {

    private final ConcurrentHashMap<UUID, ConnectionSession> sessions = new ConcurrentHashMap<>();
    private final Set<String> authorizedUsers = ConcurrentHashMap.newKeySet();
    private final int maxSessionsCount;
    private final Object createLock = new Object();

    public SessionManager(int maxSessionsCount) {
        this.maxSessionsCount = maxSessionsCount;
    }


    //TcpSessionsManager

    public TcpSession createTcpSession(AsynchronousSocketChannel userConnection) {
        synchronized (createLock) {
            TcpSessionImpl tcpSession = new TcpSessionImpl(UUID.randomUUID(), new Date(), userConnection);
            sessions.put(tcpSession.getId(), new ConnectionSession(tcpSession, null));
            return tcpSession;
        }
    }

    @Nullable
    public TcpSession tryGetTcpSession(UUID sessionId) {
        ConnectionSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        return session.getTcpSession();
    }

    @NotNull
    public TcpSession getTcpSession(UUID sessionId) throws TcpSessionNotFoundException {
        TcpSession tcpSession = tryGetTcpSession(sessionId);
        if (tcpSession == null) {
            throw new TcpSessionNotFoundException(sessionId);
        }
        return tcpSession;
    }

    public void closeTcpSession(UUID sessionId) throws IOException {
        ConnectionSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }
        synchronized (session) {
            sessions.remove(sessionId);
            logoutInternal(session);
            session.getTcpSession().close();
        }
    }

    public int getSessionsCount() {
        return sessions.size();
    }

    //UserSessionsManager

    public AuthorizationResult authorize(UUID sessionId, String userName) {

        ConnectionSession storedSession = sessions.get(sessionId);
        if (storedSession == null) {
            return new AuthorizationResult(AuthorizationResultStatus.SESSION_NOT_FOUND, null);
        }
        if (storedSession.getTcpSession().isClosed()) {
            return new AuthorizationResult(AuthorizationResultStatus.SESSION_CLOSED, null);
        }
        synchronized (storedSession) {
            UserSession userSession = storedSession.getUserSession();
            if (userSession != null) {
                return new AuthorizationResult(AuthorizationResultStatus.SUCCESS, userSession);
            }
            if (authorizedUsers.contains(userName)) {
                return new AuthorizationResult(AuthorizationResultStatus.ALREADY_AUTHORIZED, null);
            }
            userSession = new UserSession(sessionId, userName, new Date(), storedSession.getTcpSession().getCreateDate());
            storedSession.setUserSession(userSession);
            authorizedUsers.add(userName);
            return new AuthorizationResult(AuthorizationResultStatus.SUCCESS, userSession);
        }
    }

    @Override
    public @Nullable UserSession tryGet(UUID sessionId) {
        ConnectionSession session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        return session.getUserSession();
    }

    @Override
    public @NotNull UserSession get(UUID sessionId) throws UserSessionNotFoundException {
        UserSession userSession = tryGet(sessionId);
        if (userSession == null) {
            throw new UserSessionNotFoundException(sessionId);
        }
        return userSession;
    }

    @Override
    public void logout(UUID sessionId) {
        ConnectionSession session = sessions.get(sessionId);
        if (session == null) {
            return;
        }
        synchronized (session) {
            logoutInternal(session);
        }
    }

    @Override
    public int getAuthorizedUsersCount() {
        return authorizedUsers.size();
    }

    public boolean canCreateTcpSession() {
        synchronized (createLock) {
            return getSessionsCount() < maxSessionsCount;
        }
    }

    //private

    private void logoutInternal(@NotNull ConnectionSession session) {
        UserSession userSession = session.getUserSession();
        if (userSession == null) {
            return;
        }
        authorizedUsers.remove(userSession.getUserName());
        session.setUserSession(null);
    }

    private static class ConnectionSession {

        @NotNull
        private final TcpSessionImpl tcpSession;
        @Nullable
        private UserSession userSession;

        public ConnectionSession(@NotNull TcpSessionImpl tcpSession, @Nullable UserSession userSession) {
            this.tcpSession = tcpSession;
            this.userSession = userSession;
        }

        public TcpSessionImpl getTcpSession() {
            return tcpSession;
        }

        public UserSession getUserSession() {
            return userSession;
        }

        public void setUserSession(@Nullable UserSession userSession) {
            this.userSession = userSession;
        }
    }


}
