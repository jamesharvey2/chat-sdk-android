package co.chatsdk.core.dao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import co.chatsdk.core.interfaces.CoreEntity;
import co.chatsdk.core.types.ConnectionType;

@org.greenrobot.greendao.annotation.Entity
public class ContactLink implements CoreEntity {

    @Id
    private Long id;
    private Long userId;
    private Long linkOwnerBUserDaoId;


    @NotNull
    private int type;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLinkOwnerBUserDaoId() {
        return this.linkOwnerBUserDaoId;
    }

    public void setConnectionType (ConnectionType type) {
        this.type = type.ordinal();
    }

    public ConnectionType getConnectionType () {
        return ConnectionType.values()[this.type];
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setEntityID (String entityID) {

    }

    public String getEntityID () {
        return id.toString();
    }

    public void setLinkOwnerBUserDaoId(Long linkOwnerBUserDaoId) {
        this.linkOwnerBUserDaoId = linkOwnerBUserDaoId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1250968650)
    public BUser getBUser() {
        Long __key = this.userId;
        if (BUser__resolvedKey == null || !BUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BUserDao targetDao = daoSession.getBUserDao();
            BUser BUserNew = targetDao.load(__key);
            synchronized (this) {
                BUser = BUserNew;
                BUser__resolvedKey = __key;
            }
        }
        return BUser;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1505773358)
    public void setBUser(BUser BUser) {
        synchronized (this) {
            this.BUser = BUser;
            userId = BUser == null ? null : BUser.getId();
            BUser__resolvedKey = userId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2018954378)
    public BUser getLinkOwnerBUser() {
        Long __key = this.linkOwnerBUserDaoId;
        if (linkOwnerBUser__resolvedKey == null
                || !linkOwnerBUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BUserDao targetDao = daoSession.getBUserDao();
            BUser linkOwnerBUserNew = targetDao.load(__key);
            synchronized (this) {
                linkOwnerBUser = linkOwnerBUserNew;
                linkOwnerBUser__resolvedKey = __key;
            }
        }
        return linkOwnerBUser;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2037961454)
    public void setLinkOwnerBUser(BUser linkOwnerBUser) {
        synchronized (this) {
            this.linkOwnerBUser = linkOwnerBUser;
            linkOwnerBUserDaoId = linkOwnerBUser == null ? null
                    : linkOwnerBUser.getId();
            linkOwnerBUser__resolvedKey = linkOwnerBUserDaoId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 82228261)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getContactLinkDao() : null;
    }

    @ToOne(joinProperty = "userId")
    private BUser BUser;
    @ToOne(joinProperty = "linkOwnerBUserDaoId")
    private BUser linkOwnerBUser;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1108094631)
    private transient ContactLinkDao myDao;
    @Generated(hash = 2128182758)
    private transient Long BUser__resolvedKey;
    @Generated(hash = 416171141)
    private transient Long linkOwnerBUser__resolvedKey;
    @Generated(hash = 1857621306)
    public ContactLink(Long id, Long userId, Long linkOwnerBUserDaoId, int type) {
        this.id = id;
        this.userId = userId;
        this.linkOwnerBUserDaoId = linkOwnerBUserDaoId;
        this.type = type;
    }

    @Generated(hash = 48143975)
    public ContactLink() {
    }

}
