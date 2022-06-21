import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.tukaani.xz.check.None;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

class Method{
    //ont.getOntologyID().getOntologyIRI()
    public void useReasoner(String path) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);

        // use reasoner
//        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);
        // query all the all descendants of vegetarian
        OWLDataFactory fac = manager.getOWLDataFactory();

        System.out.println("Number of individuals: "
                + ont.getIndividualsInSignature().size());

        String prefix = String.valueOf(ont.getOntologyID().getOntologyIRI().get());
        prefix = prefix.substring(0,prefix.length()-1);

//        System.out.println(String.valueOf(ont.getOntologyID().getOntologyIRI().get()));
//        System.out.println("http://protege.stanford.edu/ontologies/groceries/Vitamin");


        OWLClass a = fac.getOWLClass(IRI.create(prefix+"#person"));
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(a, true);
        // print the result with flattened set
        Set<OWLClass> clses = subClses.getFlattened();
        System.out.println("Subclasses of Vitamin: ");
        for (OWLClass cls : clses) {
            System.out.println("    " + cls);
        }
        System.out.println("\n");
//        ArrayList<OWLClass> classes = new ArrayList<OWLClass>();
//        ont.classesInSignature().forEach(classes::add);
//        for (OWLClass cls : classes) {
//            System.out.println("    " + cls);
//        }
    }
    public void queryWithReasoner(String path) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);

        // use reasoner
//        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);

        // Ask the reasoner to do all the necessary work now
        reasoner.precomputeInferences();
        // We can determine if the ontology is actually consistent (in this
        // case, it should be).
        boolean consistent = reasoner.isConsistent();
        System.out.println("Consistent: " + consistent);
        System.out.println("\n");

        // get all unsatisfied classes except owl:Nothing
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.out.println("    " + cls);
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }
        System.out.println("\n");

        // query all the all descendants of vegetarian
        OWLDataFactory fac = manager.getOWLDataFactory();
        OWLClass Almond = fac.getOWLClass(IRI
                .create(ont.getOntologyID().getOntologyIRI()+"/Vitamin"));
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(Almond, true);
        // print the result with flattened set
        Set<OWLClass> clses = subClses.getFlattened();
        System.out.println("Subclasses of Vitamin: ");
        for (OWLClass cls : clses) {
            System.out.println("    " + cls);
        }
        System.out.println("\n");
    }

    private static void printProperties(OWLOntologyManager man, OWLOntology ont,
                                        OWLReasoner reasoner, OWLClass cls) {
//        if (!ont.containsClassInSignature(cls.getIRI())) {
//            throw new RuntimeException("Class not in signature of the ontology");
//        }
        // Note that the following code could be optimised... if we find that
        // instances of the specified class do not have a property, then we
        // don't need to check the sub properties of this property
        System.out.println("Properties of " + cls);
        for (OWLObjectPropertyExpression prop : ont.getObjectPropertiesInSignature()) {
            boolean sat = hasProperty(man, reasoner, cls, prop);
            if (sat) {
                System.out.println("Instances of " + cls
                        + " necessarily have the property " + prop);
            }
        }
    }
    private static boolean hasProperty(OWLOntologyManager man, OWLReasoner reasoner,
                                       OWLClass cls, OWLObjectPropertyExpression prop) {
        // To test whether the instances of a class must have a property we
        // create a some values from restriction and then ask for the
        // satisfiability of the class interesected with the complement of this
        // some values from restriction. If the intersection is satisfiable then
        // the instances of the class don't have to have the property,
        // otherwise, they do.
        OWLDataFactory dataFactory = man.getOWLDataFactory();
        OWLClassExpression restriction = dataFactory.getOWLObjectSomeValuesFrom(prop,
                dataFactory.getOWLThing());
        // Now we see if the intersection of the class and the complement of
        // this restriction is satisfiable
        OWLClassExpression complement = dataFactory.getOWLObjectComplementOf(restriction);
        OWLClassExpression intersection = dataFactory.getOWLObjectIntersectionOf(cls,
                complement);
        return !reasoner.isSatisfiable(intersection);
    }

    public void SubsumptionChecking(String path) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);
    }

    public void DLQueryExample(String path) throws OWLOntologyCreationException, IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            File file = new File(path);
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);
            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
            OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);
//            reasoner.isEntailed()
            ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
            DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(
                    reasoner, shortFormProvider), shortFormProvider);
            doQueryLoop(dlQueryPrinter);
        }catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        } catch (IOException ioEx) {
            System.out.println(ioEx.getMessage());
        }

    }

    public void ELSubsumptionChecking(String path) throws OWLOntologyCreationException, IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            File file = new File(path);
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(file);
            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
            OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);
//            reasoner.isEntailed()
            ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
            DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(
                    reasoner, shortFormProvider), shortFormProvider);
            doCheckLoop(dlQueryPrinter);
        }catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        } catch (IOException ioEx) {
            System.out.println(ioEx.getMessage());
        }

    }

    private static void doCheckLoop(DLQueryPrinter dlQueryPrinter) throws IOException{
        while(true){
            System.out
                    .println("Please type a Policy in Manchester Syntax and press Enter (or press x to exit):");
            System.out.println("");
            String classExpression1 = readInput();
            if (classExpression1.equalsIgnoreCase("x")) {
                break;
            }
            System.out
                    .println("Please type a Concept in Manchester Syntax and press Enter (or press x to exit):");
            System.out.println("");
            String classExpression2 = readInput();
            if (classExpression2.equalsIgnoreCase("x")) {
                break;
            }
            if(subsumptionChecking(dlQueryPrinter,classExpression1, classExpression2)){
                System.out.println("Yes, policy subsumes concepts.");
            }
            else{
                System.out.println("No, no subsumption relationship.");
                }
            System.out.println();
            System.out.println();
        }
    }

    private static boolean subsumptionChecking(DLQueryPrinter dlQueryPrinter, String classExpression1,String classExpression2){
        // Policy
        Set<OWLClass> directSubClass1 = dlQueryPrinter.getSubClasses(classExpression1,true);
        Set<OWLClass> allSubClass1 = dlQueryPrinter.getSubClasses(classExpression1,false);
        Set<OWLClass> directSuperClass1 = dlQueryPrinter.getSuperClasses(classExpression1,true);
        Set<OWLNamedIndividual> individual1 = dlQueryPrinter.getIndividual(classExpression1,true);
        OWLClass class1 = dlQueryPrinter.getOWLClass(classExpression1);

        // Concept
        Set<OWLClass> directSubClass2 = dlQueryPrinter.getSubClasses(classExpression2, true);
        Set<OWLClass> allSuperClass2 = dlQueryPrinter.getSuperClasses(classExpression2,false);
        Set<OWLClass> directSuperClass2 = dlQueryPrinter.getSuperClasses(classExpression2,true);
        Set<OWLNamedIndividual> individual2 = dlQueryPrinter.getIndividual(classExpression2,true);
        OWLClass class2 = dlQueryPrinter.getOWLClass(classExpression2);
        // policy's allsubclasses contain concept's direct superclass
        if (allSubClass1.containsAll(directSuperClass2)){
            System.out.println(1);
            return true;
        }
        // policy's allsubclasses contain concept's class
        if (class2!=null && allSubClass1.contains(class2)){
            System.out.println(2);
            return true;
        }
        // concepts's allsuperclasses contain any one of policy's direct subclasses
        for (OWLClass dsc1:directSubClass1){
            if (allSuperClass2.contains(dsc1)){
                System.out.println(3);
                return true;
            }
        }
        // concepts's allsuperclasses contain policy's class
        if (class1!=null && allSuperClass2.contains(class1)){
            System.out.println(4);
            return true;
        }
        // when policy and concept are at similar hierarchy position
        if (directSuperClass2.containsAll(directSuperClass1) && directSubClass2.containsAll(directSubClass1)
                && individual1.containsAll(individual2) && !individual1.isEmpty() && !individual2.isEmpty()){
            System.out.println(5);
            return true;
        }
        return false;
    }

    private static void doQueryLoop(DLQueryPrinter dlQueryPrinter) throws IOException {
        while (true) {
            // Prompt the user to enter a class expression
            System.out
                    .println("Please type a class expression in Manchester Syntax and press Enter (or press x to exit):");
            System.out.println("");
            String classExpression = readInput();
            // Check for exit condition
            if (classExpression.equalsIgnoreCase("x")) {
                break;
            }
            dlQueryPrinter.askQuery(classExpression.trim());
            System.out.println();
            System.out.println();
        }
    }

    private static String readInput() throws IOException {
        InputStream is = System.in;
        InputStreamReader reader;
        reader = new InputStreamReader(is, "UTF-8");
        BufferedReader br = new BufferedReader(reader);
        return br.readLine();
    }

}


class DLQueryParser {
    private final OWLOntology rootOntology;
    private final BidirectionalShortFormProvider bidiShortFormProvider;
    public DLQueryParser(OWLOntology rootOntology, ShortFormProvider shortFormProvider) {
        this.rootOntology = rootOntology;
        OWLOntologyManager manager = rootOntology.getOWLOntologyManager();
        Set<OWLOntology> importsClosure = rootOntology.getImportsClosure();
        // Create a bidirectional short form provider to do the actual mapping.
        // It will generate names using the input
        // short form provider.
        bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager,
                importsClosure, shortFormProvider);
    }

    public OWLClassExpression parseClassExpression(String classExpressionString){
        OWLDataFactory dataFactory = rootOntology.getOWLOntologyManager()
                .getOWLDataFactory();
        // Set up the real parser
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
                dataFactory, classExpressionString);
        parser.setDefaultOntology(rootOntology);
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        parser.setOWLEntityChecker(entityChecker);
        return parser.parseClassExpression();
    }
}

class DLQueryEngine {
    private final OWLReasoner reasoner;
    private final DLQueryParser parser;

    // The reasoner to be used for answering the queries.
    public DLQueryEngine(OWLReasoner reasoner, ShortFormProvider shortFormProvider) {
        this.reasoner = reasoner;
        OWLOntology rootOntology = reasoner.getRootOntology();
        parser = new DLQueryParser(rootOntology, shortFormProvider);
    }


    public Set<OWLClass> getSuperClasses(String classExpressionString, boolean direct)
            throws ParserException {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> superClasses = reasoner
                .getSuperClasses(classExpression, direct);
        return superClasses.getFlattened();
    }
    public Set<OWLClass> getEquivalentClasses(String classExpressionString)
            throws ParserException {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(classExpression);
        Set<OWLClass> result;
        if (classExpression.isAnonymous()) {
            result = equivalentClasses.getEntities();
        } else {
            result = equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
        }
        return result;
    }

    public OWLClass getOWLClass(String classExpressionString){
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        try {
            return classExpression.asOWLClass();
        }catch(Exception e){
            return null;
        }

    }

    public Set<OWLClass> getSubClasses(String classExpressionString, boolean direct)
            throws ParserException {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(classExpression, direct);
        return subClasses.getFlattened();
    }

    public Set<OWLNamedIndividual> getInstances(String classExpressionString,
                                                boolean direct) throws ParserException {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression,
                direct);
        return individuals.getFlattened();
    }
}

class DLQueryPrinter {
    private final DLQueryEngine dlQueryEngine;
    private final ShortFormProvider shortFormProvider;

    public DLQueryPrinter(DLQueryEngine engine, ShortFormProvider shortFormProvider) {
        this.shortFormProvider = shortFormProvider;
        dlQueryEngine = engine;
    }

    public void askQuery(String classExpression) {
        if (classExpression.length() == 0) {
            System.out.println("No class expression specified");
        } else {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("\n--------------------------------------------------------------------------------\n");
                sb.append("QUERY:   ");
                sb.append(classExpression);
                sb.append("\n");
                sb.append("--------------------------------------------------------------------------------\n\n");
                // Ask for the subclasses, superclasses etc. of the specified
                // class expression. Print out the results.
                Set<OWLClass> superClasses = dlQueryEngine.getSuperClasses(
                        classExpression, true);
                printEntities("SuperClasses", superClasses, sb);
                Set<OWLClass> equivalentClasses = dlQueryEngine
                        .getEquivalentClasses(classExpression);
                printEntities("EquivalentClasses", equivalentClasses, sb);
                Set<OWLClass> subClasses = dlQueryEngine.getSubClasses(classExpression,
                        true);
                printEntities("SubClasses", subClasses, sb);
                Set<OWLNamedIndividual> individuals = dlQueryEngine.getInstances(
                        classExpression, true);
                printEntities("Instances", individuals, sb);
                 System.out.println(sb.toString());
            } catch (ParserException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public Set<OWLClass> getSuperClasses(String classExpression, boolean direct){
        Set<OWLClass> superClasses = dlQueryEngine.getSuperClasses(
                classExpression, direct);
        return superClasses;
    }

    public Set<OWLClass> getSubClasses(String classExpression, boolean direct){
        Set<OWLClass> subClasses = dlQueryEngine.getSubClasses(classExpression,
                direct);
        return subClasses;
    }

    public Set<OWLNamedIndividual> getIndividual(String classExpression, boolean direct){
        Set<OWLNamedIndividual> individuals = dlQueryEngine.getInstances(
                classExpression, direct);
        return individuals;
    }

    public Set<OWLClass> getEquivalentClasses(String classExpression){
        Set<OWLClass> equivalentClasses = dlQueryEngine
                .getEquivalentClasses(classExpression);
        return equivalentClasses;
    }

    public OWLClass getOWLClass(String classExpression){
        OWLClass cls = dlQueryEngine.getOWLClass(classExpression);
        return cls;
    }

    private void printEntities(String name, Set<? extends OWLEntity> entities,
                               StringBuilder sb) {
        sb.append(name);
        int length = 50 - name.length();
        for (int i = 0; i < length; i++) {
            sb.append(".");
        }
        sb.append("\n\n");
        if (!entities.isEmpty()) {
            for (OWLEntity entity : entities) {
                sb.append("\t");
                sb.append(shortFormProvider.getShortForm(entity));
                sb.append("\n");
            }
        } else {
            sb.append("\t[NONE]\n");
        }
        sb.append("\n");
    }
}


public class query {
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
//        String healthDataOntology = "/pythonProject/Msc Project/posc-groceries.owl";
//        String healthDataOntology = "/pythonProject/Msc Project/health-data.owl";
        String healthDataOntology = "/javaProject/hermit/health-data1.owl";
        Method m = new Method();
//        m.ELSubsumptionChecking(healthDataOntology);
        m.DLQueryExample(healthDataOntology);
    }

}
