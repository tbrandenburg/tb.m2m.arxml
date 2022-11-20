package tb.m2m.arxml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.artop.aal.common.resource.impl.AutosarResourceFactoryImpl;
import org.artop.aal.common.resource.impl.AutosarResourceSetImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;

import autosar40.genericstructure.generaltemplateclasses.arobject.ARObject;
import autosar40.genericstructure.generaltemplateclasses.identifiable.Referrable;
import autosar40.swcomponent.components.PPortPrototype;
import autosar40.swcomponent.components.PortPrototype;
import autosar40.swcomponent.components.RPortPrototype;
import autosar40.util.Autosar40Package;
import autosar40.util.Autosar40ReleaseDescriptor;
import autosar40.util.Autosar40ResourceFactoryImpl;
import gautosar.ggenericstructure.ginfrastructure.GARPackage;
import gautosar.gswcomponents.gcomponents.GPPortPrototype;
import gautosar.gswcomponents.gportinterface.GPortInterface;

public class ARXMLReader {

	public void setup() {
		@SuppressWarnings("unused")
		Autosar40Package einstance = Autosar40Package.eINSTANCE;

		AutosarResourceFactoryImpl resourceFactory = new Autosar40ResourceFactoryImpl();

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("arxml", resourceFactory);

		MetaModelDescriptorRegistry.INSTANCE.addDescriptor(Autosar40ReleaseDescriptor.INSTANCE);
	}

	private void printTree(EList<EObject> eList, String prefix) {
		for (EObject e: eList) {
			if(e instanceof Referrable) {
				System.out.println(prefix + "Referrable: " + ((Referrable)e).getShortName() + " (" + e.eClass().getName() + ")");
			} else if(e instanceof ARObject) {
				System.out.println(prefix + "ARObject: " + ((ARObject)e).eClass().getName());
			}
			if(e instanceof RPortPrototype) {
				if(((RPortPrototype)e).getRequiredInterface() != null && ((RPortPrototype)e).getRequiredInterface().getShortName() != "") {
					System.out.println(prefix + "RPort: " + ((RPortPrototype)e).getShortName() + " -> " + ((RPortPrototype)e).getRequiredInterface().getShortName());					
				} else {
					System.out.println(prefix + "RPort: " + ((RPortPrototype)e).getShortName() + " -> ?");		
				}
			}
			if(e instanceof PPortPrototype) {
				if(((PPortPrototype)e).getProvidedInterface() != null && ((PPortPrototype)e).getProvidedInterface().getShortName() != "") {
					System.out.println(prefix + "RPort: " + ((PPortPrototype)e).getShortName() + " -> " + ((PPortPrototype)e).getProvidedInterface().getShortName());					
				} else {
					System.out.println(prefix + "RPort: " + ((PPortPrototype)e).getShortName() + " -> ?");		
				}
			}
			printTree(e.eContents(), prefix + "  ");
		}
	}

	private void loadARXML(ResourceSet resourceSet, String filePath) {
		Map options = new HashMap();

		File dir = new File(filePath);

		URI arxml = URI.createFileURI(dir.getAbsolutePath());
		Resource resource = resourceSet.getResource(arxml, true);
	}


	public void execute(String path) throws IOException {
		ResourceSet resourceSet = new MyAutosarResourceSetImpl();
		
		List<String> fileList = new ArrayList<String>();
		
		try (Stream<Path> walkStream = Files.walk(Paths.get(path))) {
		    walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
		        if (f.toString().endsWith("arxml")) {
		        	System.out.println("Loading " + f.toAbsolutePath().toString() + "...");
		        	this.loadARXML(resourceSet, f.toAbsolutePath().toString());
		        }
		    });
		}
		
		for (Resource res: resourceSet.getResources()) {
			if(res.isLoaded()) {
				this.printTree(res.getContents(),"");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			ARXMLReader arxmlReader = new ARXMLReader();
			arxmlReader.setup();
			arxmlReader.execute(new File(args[0]).getPath());
		}
	}


}