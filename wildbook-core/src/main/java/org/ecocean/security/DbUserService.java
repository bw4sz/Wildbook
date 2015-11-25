package org.ecocean.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.ecocean.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

public class DbUserService implements UserService {
    private final static Logger logger = LoggerFactory.getLogger(DbUserService.class);

    private final ConnectionInfo ci;
    private final Map<Integer, SecurityInfo> mapUserId = new HashMap<>();
    private final Map<String, SecurityInfo> mapUserName = new HashMap<>();
    private final Map<String, SecurityInfo> mapUserEmail = new HashMap<>();
    private List<Organization> orgs;

    public DbUserService(final ConnectionInfo ci) {
        this.ci = ci;
    }

    private SecurityInfo addNewSecurityInfo(final User user) {
        SecurityInfo info = new SecurityInfo(user);
        mapUserId.put(user.getUserId(), info);
        if (user.getUsername() != null) {
            mapUserName.put(user.getUsername().toLowerCase(), info);
        }
        mapUserEmail.put(user.getEmail().toLowerCase(), info);

        try (Database db = new Database(ci)) {
            db.getTable(UserFactory.TABLENAME_ROLES).select((rs) -> {
                String aContext = rs.getString("context");
                Set<String> someRoles = info.getContextRoles(aContext);
                if (someRoles == null) {
                    someRoles = new HashSet<>();
                    info.setContextRoles(aContext, someRoles);
                }
                someRoles.add(rs.getString("rolename"));
            }, "userid = " + user.getUserId());
        } catch(DatabaseException ex){
            logger.error("Can't read roles", ex);
        }

        return info;
    }

    @Override
    public SecurityInfo getSecurityInfo(final String userIdString) {
        Integer userid = NumberUtils.createInteger(userIdString);

        SecurityInfo info = mapUserId.get(userid);

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user = UserFactory.getUserById(db, userid);
                if (user == null) {
                    throw new SecurityException("No account found for user [" + userid + "]");
                }
                info = addNewSecurityInfo(user);
            } catch (DatabaseException ex) {
                throw new SecurityException("Trouble authenticating user [" + userid + "]", ex);
            }
        }

        return info;
    }

    @Override
    public User getUserById(final String id) {
        return getSecurityInfo(id).getUser();
    }

    @Override
    public Set<String> getAllRolesForUserInContext(final String id, final String context) {
        SecurityInfo info = getSecurityInfo(id);
        return info.getContextRoles(context);
    }

    @Override
    public List<Organization> getOrganizations() {
        if (orgs == null) {
            try (Database db = new Database(ci)){
                orgs = UserFactory.getOrganizations(db);
            } catch (DatabaseException ex) {
                throw new SecurityException("Can't read organizations", ex);
            }
        }

        return orgs;
    }

    @Override
    public User getUserByEmail(final String email) {
        if (email == null) {
            return null;
        }

        SecurityInfo info = mapUserEmail.get(email.toLowerCase());

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user;
                try {
                    user = UserFactory.getUserByEmail(db, email);
                } catch (DatabaseException ex) {
                    throw new SecurityException("Can't read user.", ex);
                }
                if (user == null) {
                    return null;
                }
                info = addNewSecurityInfo(user);
            }
        }

        return info.getUser();
    }

    @Override
    public User getUserByNameOrEmail(final String term) {
        if (term == null) {
            return null;
        }

        SecurityInfo info = mapUserEmail.get(term.toLowerCase());

        if (info != null) {
            return info.getUser();
        }

        info = mapUserName.get(term.toLowerCase());
        if (info != null) {
            return info.getUser();
        }

        if (info == null) {
            try (Database db = new Database(ci)) {
                User user;
                try {
                    user = UserFactory.getUserByNameOrEmail(db, term);
                } catch (DatabaseException ex) {
                    throw new SecurityException("Can't read user.", ex);
                }
                if (user == null) {
                    return null;
                }
                info = addNewSecurityInfo(user);
            }
        }

        return info.getUser();
    }

    @Override
    public String createPWResetToken(final String userid) {
        try (Database db = new Database(ci)) {
            return UserFactory.createPWResetToken(db, NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't reset password token for user [" + userid + "]");
        }
    }

    @Override
    public void saveUser(final User user) {
        try (Database db = new Database(ci)) {
            UserFactory.saveUser(db, user);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't save user [" + user.getUserId() + "]");
        }
    }

    @Override
    public void deleteRoles(final String userid) {
        try (Database db = new Database(ci)) {
            UserFactory.deleteRoles(db, NumberUtils.createInteger(userid));
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]");
        }
    }

    @Override
    public void addRole(final String userid, final String context, final String role) {
        try (Database db = new Database(ci)) {
            UserFactory.addRole(db, NumberUtils.createInteger(userid), context, role);
        } catch (DatabaseException ex) {
            throw new SecurityException("Can't delete roles from user [" + userid + "]");
        }
    }

    @Override
    public User verifyPRToken(final String token) {
        try (Database db = new Database(ci)) {
            return UserFactory.verifyPRToken(db, token);
        } catch (DatabaseException | IllegalAccessException ex) {
            throw new SecurityException("Can't find user for token [" + token + "]");
        }
    }

    @Override
    public boolean doesUserHaveRole(final String userid, final String context, final String role) {
        SecurityInfo info = getSecurityInfo(userid);
        if (info == null) {
            return false;
        }
        Set<String> roles = info.getContextRoles(context);
        if (roles == null) {
            return false;
        }
        return roles.contains(role);
    }
}