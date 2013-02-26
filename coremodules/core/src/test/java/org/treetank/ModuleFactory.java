package org.treetank;

import java.io.File;
import java.util.Map;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.io.IBackend;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ModuleFactory implements IModuleFactory {

    private final static String NODEFACTORYPARAMETER = "NodeFactory";
    private final static String METAFACTORYPARAMETER = "MetaFactory";
    private final static String REVISIONINGPARAMETER = "Revisioning";
    private final static String BACKENDPARAMETER = "Backend";

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Module createModule(ITestContext context, Class<?> testClass) {
        AbstractModule returnVal = null;
        String nodeFacName;
        String metaFacName;
        String revisioningName = "org.treetank.revisioning.SlidingSnapshot";
        String backendName = "org.treetank.io.combinedCloud.CombinedBackend";
        // getting the parameters over testng.xml and setting it directly or...
        if (context.getSuite().getParameter(NODEFACTORYPARAMETER) != null) {
            final Map<String, String> params = context.getSuite().getXmlSuite().getAllParameters();
            nodeFacName = params.get(NODEFACTORYPARAMETER);
            metaFacName = params.get(METAFACTORYPARAMETER);
            revisioningName = params.get(REVISIONINGPARAMETER);
            backendName = params.get(BACKENDPARAMETER);
        }// ..determining standard factories based on bundle parsed from the testclass and...
        else {
            final String[] elements =
                testClass.getProtectionDomain().getCodeSource().getLocation().toString()
                    .split(File.separator);
            String module = elements[elements.length - 3];
            switch (module) {
            case "core":
                nodeFacName = "org.treetank.page.DumbNodeFactory";
                metaFacName = "org.treetank.page.DumbMetaEntryFactory";
                break;
            case "node":
            case "xml":
            case "saxon":
            case "jax-rx":
                nodeFacName = "org.treetank.node.TreeNodeFactory";
                metaFacName = "org.treetank.node.NodeMetaPageFactory";
                break;
            case "iscsi":
                nodeFacName = "org.treetank.iscsi.node.ByteNodeFactory";
                metaFacName = "org.treetank.iscsi.node.ISCSIMetaPageFactory";
                break;
            case "filelistener":
                nodeFacName = "org.treetank.filelistener.file.node.FileNodeFactory";
                metaFacName = "org.treetank.filelistener.file.node.FilelistenerMetaPageFactory";
                break;
            default:
                throw new IllegalStateException("Suitable module not found");
            }

        }
        // ...invoking it over reflection and setting it to the ModuleSetter.
        Class<INodeFactory> nodeFac;
        Class<IMetaEntryFactory> metaFac;
        Class<IRevisioning> revisioning;
        Class<IBackend> backend;
        try {
            nodeFac = (Class<INodeFactory>)Class.forName(nodeFacName);
            metaFac = (Class<IMetaEntryFactory>)Class.forName(metaFacName);
            revisioning = (Class<IRevisioning>)Class.forName(revisioningName);
            backend = (Class<IBackend>)Class.forName(backendName);
        } catch (ClassNotFoundException exc) {
            throw new RuntimeException(exc);
        }

        returnVal =
            new ModuleSetter().setNodeFacClass(nodeFac).setMetaFacClass(metaFac)
                .setRevisingClass(revisioning).setBackendClass(backend).createModule();

        return returnVal;

        // String suiteName = context.getSuite().getName();
        // switch (suiteName) {
        // case "JCloudsZipper":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
        // IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "JCloudsEncryptor":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, JCloudsStorage.class).build(
        // IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "BerkeleyZipper":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
        // .build(IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;
        // case "BerkeleyEncryptor":
        // returnVal = new AbstractModule() {
        //
        // @Override
        // protected void configure() {
        // bind(IRevisioning.class).to(Differential.class);
        // bind(INodeFactory.class).to(DumbNodeFactory.class);
        // bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Encryptor()));
        //
        // install(new FactoryModuleBuilder().implement(IBackend.class, BerkeleyStorage.class)
        // .build(IBackendFactory.class));
        //
        // install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        //
        // bind(Key.class).toInstance(StandardSettings.KEY);
        // install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
        // }
        // };
        // break;

        // default:
        // break;
        // }

    }
}
