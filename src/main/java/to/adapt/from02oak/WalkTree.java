package to.adapt.from02oak;

import org.apache.jackrabbit.oak.commons.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.jcr.*;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalkTree {
    private final static Logger LOG = LoggerFactory.getLogger(WalkTree.class);

    private WalkTree() {
    }

    public static void main (String... args) throws IOException {
        Repository repo = Repository.getRepo();
        Session session = null;

        try {
            // Loggin in
            session = repo.login(Repository.ADMIN);
            LOG.debug("Logged in: {}", session);

            // simple tree walking
            walk(session.getRootNode());

        } catch (RepositoryException e) {
            LOG.error("Error while performing repository operations.", e);
        } finally {
            session.logout();
        }
        repo.close();
    }

    private static void walk(@Nonnull Node node) throws RepositoryException {
        checkNotNull(node);
        String p = node.getPath();
        int depth = PathUtils.getDepth(p);


        // building the depth
        String dt = "";
        if (depth == 0) {
            LOG.info("{} {}", p, node.getName());
        } else {
            for (int i = 0; i < depth; i++) {
                dt += "|";
            }
            LOG.info("{}-> {}", dt, node.getName());
        }

        final String depthTree = dt;

        // dumping properties
        node.getProperties().forEachRemaining(property -> {
            try {
                Property pp = (Property) property;
                LOG.info("{}- {}: {}", depthTree, pp.getName(), (pp.isMultiple() ? pp.getValues() : pp.getValue()));
            } catch (RepositoryException e) {
                LOG.error("Error while dumping property", e);
            }
        });

        // dumping subnodes
        node.getNodes().forEachRemaining(n -> {
            try {
                walk((Node) n);
            } catch (RepositoryException e) {
                LOG.error("Error dumping nodes", e);
            }
        });
    }
}