/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package to.adapt.from02oak.repository;

import com.google.common.collect.ImmutableList;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.blob.datastore.DataStoreBlobStore;
import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.apache.jackrabbit.oak.plugins.index.IndexUtils;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexEditorProvider;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.lifecycle.RepositoryInitializer;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.initialisers.BunchOfColours;

import javax.annotation.Nonnull;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.of;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.apache.jackrabbit.oak.api.Type.NAME;
import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.INDEX_DEFINITIONS_NODE_TYPE;
import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.PROPERTY_NAMES;
import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.TYPE_PROPERTY_NAME;

/**
 * Holds the state for the repository and act as a gateway for all major operations
 *
 * Invoke Repository.close() to properly shutdown the repository
 */
public class Repository implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Repository.class);
    public final static Credentials ADMIN = new SimpleCredentials("admin", "admin".toCharArray());

    /**
     * path to the repository root on disk
     */
    private static final String REPO_PATH = "repository";

    /**
     * path to the location on disk for the segment store
     */
    private static final String SEGMENT_PATH = REPO_PATH + "/segment-tar";

    /**
     * path for the location on disk for the {@link org.apache.jackrabbit.oak.spi.blob.BlobStore}
     */
    private static final String DS_PATH = REPO_PATH + "/datastore";

    /**
     * static instance of an initialised repository
     */
    private static Repository repo;

    private NodeStore store;
    private FileStore fileStore;
    private javax.jcr.Repository jcrRepo;

    Repository() throws IOException {
        initialiseRepo(initialiseSegmentStore());
    }

    Repository(@Nonnull NodeStore store) {
        initialiseRepo(store);
    }

    private void initialiseRepo(@Nonnull NodeStore s) {
        this.store = checkNotNull(s);
        LOG.debug("NodeStore initialised. {}", store);

        LOG.debug("Initialising Jcr Content Repository");
        Jcr jcr = new Jcr(store);

        // initialising property index - http://jackrabbit.apache.org/oak/docs/query/property-index.html
        jcr.with(new PropertyIndexEditorProvider());
        jcr.with(new RepositoryInitializer() {
            @Override
            public void initialize(@Nonnull NodeBuilder builder) {
                NodeBuilder index = builder.getChildNode("oak:index").getChildNode("colour");
                if (index.exists()) {
                    return;
                }

                LOG.debug("Property Index not found defining `colour`");

                index = IndexUtils.getOrCreateOakIndex(builder);
                IndexUtils.createIndexDefinition(index, "colour", true, false, of("colour"), null);
            }
        });

        // initialising a bunch of nodes
        jcr.with(new BunchOfColours());

        jcrRepo = jcr.createRepository();
        LOG.debug("Jcr Content Repository initialised. {}", jcrRepo);
    }

    /**
     * initialise a Segment Store
     *
     * @return
     * @throws IOException
     */
    private NodeStore initialiseSegmentStore() throws IOException {
        // initialising repo root on FS
        File f = new File(REPO_PATH);
        if (!f.exists()) {
            f.mkdir();
        }

        // initialising datastore on FS
        BlobStore blob;
        File blobDir = new File(DS_PATH);
        if (!blobDir.exists()) {
            blobDir.mkdir();
        }
        FileDataStore fileDataStore = new FileDataStore();
        fileDataStore.setPath(blobDir.getAbsolutePath());
        fileDataStore.init(null);
        blob = new DataStoreBlobStore(fileDataStore);

        LOG.debug("Initalsing the NodeStore");
        FileStoreBuilder fileBuilder = FileStoreBuilder.fileStoreBuilder(new File(SEGMENT_PATH)).withBlobStore(blob);
        try {
            fileStore = fileBuilder.build();
        } catch (InvalidFileStoreVersionException e) {
            LOG.error("Error initialising the repository", e);
            throw new IOException(e);
        }
        return SegmentNodeStoreBuilders.builder(fileStore).build();
    }

    /**
     * Initialise and return the repository.
     *
     * <strong>Remember to call {@link #close()} once done to properly shutdown</strong>
     *
     * @return an instance of configured repository. {@code null} in case of errors.
     */
    public static synchronized Repository getRepo() {
        if (repo == null) {
            try {
                repo = new Repository();
            } catch (IOException e) {
                LOG.error("Unable to instantiate the repository.", e);
            }
        }
        return repo;
    }

    /**
     * used for testing only. Avoid production usage
     * @param r
     */
    static void setRepo(@Nonnull Repository r) {
        repo = checkNotNull(r);
    }

    @Override
    public void close() throws IOException {
        if (fileStore != null) {
            LOG.debug("Closing down underlying FileStore. {}", fileStore);
            fileStore.close();
        }
    }

    public Session login(@Nonnull String username, @Nonnull String password) throws RepositoryException {
        return login(new SimpleCredentials(checkNotNull(username), checkNotNull(password).toCharArray()));
    }

    public Session login(@Nonnull Credentials credentials) throws RepositoryException {
        return jcrRepo.login(credentials);
    }
}
