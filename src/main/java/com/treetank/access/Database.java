package com.treetank.access;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EStoragePaths;

/**
 * This class represents one concrete database for enabling several
 * {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class Database implements IDatabase {

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, Database> DATABASEMAP = new ConcurrentHashMap<File, Database>();

    /** Queue with all session registered */
    private final ISession mSessions;

    /** DatabaseConfiguration with fixed settings */
    private final DatabaseConfiguration mDatabaseConfiguration;

    /**
     * Private constructor
     */
    private Database(final DatabaseConfiguration databaseConf,
            final SessionConfiguration sessionConf) throws TreetankException {
        this.mDatabaseConfiguration = databaseConf;
        checkStorage();
        mSessions = new Session(databaseConf, sessionConf);
    }

    /**
     * Creating a database. This includes loading the database configurations,
     * building up the structure and preparing everything for login.
     * 
     * @param conf
     *            which are used for the database
     * @return true of creation is valid, false otherwise
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    public static boolean createDatabase(final DatabaseConfiguration conf)
            throws TreetankIOException {
        try {
            final File file = conf.getFile();
            boolean returnVal = true;
            if (file.exists()) {
                returnVal = false;
            } else {
                returnVal = file.mkdirs();
                if (returnVal) {
                    for (EStoragePaths paths : EStoragePaths.values()) {
                        if (paths.isFolder()) {
                            returnVal = paths.getFile().mkdir();
                        } else {
                            returnVal = paths.getFile().createNewFile();
                        }
                        if (!returnVal) {
                            break;
                        }
                    }
                }
            }
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                recursiveDelete(file);
            }
            return returnVal;
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * Truncate a database. This deletes all relevant data. If there are
     * existing sessions against this database, the method returns null.
     * 
     * @param file
     *            the database at this path should be deleted.
     * @return true if removal is successful, false otherwise
     */
    public static boolean truncateDatabase(final File file) {
        if (DATABASEMAP.containsKey(file)) {
            return false;
        } else {
            return recursiveDelete(file);
        }
    }

    /**
     * Open database. A database can be opened only one. Afterwards the
     * singleton instance bound to the File is given back.
     * 
     * @param file
     *            where the database is located
     * @return {@link IDatabase} instance.
     * @throws TreetankException
     *             if something odd happens
     */
    public static IDatabase openDatabase(final File file)
            throws TreetankException {
        if (!file.exists() && !createDatabase(new DatabaseConfiguration(file))) {
            throw new TreetankUsageException(new StringBuilder(
                    "DB could not be created at location ").append(file)
                    .toString());
        }
        return DATABASEMAP.putIfAbsent(file, new Database(
                new DatabaseConfiguration(file), new SessionConfiguration()));
    }

    /**
     * This method forces the Database to close an existing instance.
     * 
     * @param file
     *            where the database should be closed
     * @throws TreetankException
     *             if something weird happens while closing
     */
    public static void forceCloseDatabase(final File file)
            throws TreetankException {
        final IDatabase database = DATABASEMAP.remove(file);
        if (database != null) {
            database.close();
        }
    }

    /**
     * Closing a database. All {@link ISession} instances within this database
     * are closed.
     * 
     * @param db
     *            database to be closed
     * @return the database
     * @throws TreetankException
     */
    public void close() throws TreetankException {
        mSessions.close();
        DATABASEMAP.remove(getFile(), this);
    }

    public ISession getSession() {
        return mSessions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return mDatabaseConfiguration.getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getVersion() {
        final int[] versions = new int[3];
        versions[0] = Integer.parseInt(mDatabaseConfiguration.getProps()
                .getProperty(EDatabaseSetting.VERSION_MAJOR.name()));
        versions[1] = Integer.parseInt(mDatabaseConfiguration.getProps()
                .getProperty(EDatabaseSetting.VERSION_MINOR.name()));
        versions[2] = Integer.parseInt(mDatabaseConfiguration.getProps()
                .getProperty(EDatabaseSetting.VERSION_FIX.name()));
        return versions;
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param file
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    private static boolean recursiveDelete(final File file) {
        if (file.isDirectory()) {
            for (final File child : file.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    private void checkStorage() throws TreetankUsageException {
        final int compareStructure = EStoragePaths.compareStructure(getFile());
        if (compareStructure != 0) {
            throw new TreetankUsageException(
                    new StringBuilder(
                            "Storage has no valid storage structure. Compared to the specification, storage has ")
                            .append(compareStructure).append(" elements!")
                            .toString());
        }
        final int[] versions = new int[3];
        versions[0] = Integer.parseInt(EDatabaseSetting.VERSION_MAJOR
                .getStandardProperty());
        versions[1] = Integer.parseInt(EDatabaseSetting.VERSION_MINOR
                .getStandardProperty());
        versions[2] = Integer.parseInt(EDatabaseSetting.VERSION_FIX
                .getStandardProperty());
        final int[] storedVersions = getVersion();
        if (storedVersions[0] < versions[0]) {
            throw new TreetankUsageException(new StringBuilder(
                    "Version Major expected: ").append(storedVersions[0])
                    .append(" but was ").append(versions[0]).toString());
        } else {
            if (storedVersions[1] < versions[1]) {
                throw new TreetankUsageException(new StringBuilder(
                        "Version Minor expected: ").append(storedVersions[1])
                        .append(" but was ").append(versions[1]).toString());
            } else {
                if (storedVersions[2] < versions[2]) {
                    throw new TreetankUsageException(new StringBuilder(
                            "Version Fix expected: ").append(storedVersions[2])
                            .append(" but was ").append(versions[2]).toString());
                }
            }
        }

    }

}
