import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.semanticweb.owlapi.vocab.OWLFacet.MAX_EXCLUSIVE;
import static org.semanticweb.owlapi.vocab.OWLFacet.MIN_INCLUSIVE;

class Ontology{
    public void loadOntology(String path) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file);
        IRI documentIRI = manager.getOntologyDocumentIRI(localPizza);
        System.out.println("    from: " + documentIRI);
        System.out.println("Loaded ontology: " + localPizza);
    }

    public void saveOntology(String path) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        // can't run
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file1 = new File(path);
        OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file1);
        IRI documentIRI = manager.getOntologyDocumentIRI(localPizza);
        System.out.println("Loaded ontology: " + localPizza);

        File file = File.createTempFile("owlapiexamples", "saving");
        manager.saveOntology(localPizza, IRI.create(file.toURI()));
        OWLOntologyFormat format = (OWLOntologyFormat) manager.getOntologyFormat(localPizza);
        OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
        manager.saveOntology(localPizza, owlxmlFormat, IRI.create(file.toURI()));
        @SuppressWarnings("unused")
        OWLOntologyDocumentTarget documentTarget = new SystemOutDocumentTarget();
        ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
        manager.saveOntology(localPizza, manSyntaxFormat, new StreamDocumentTarget(
                new ByteArrayOutputStream()));
        file.delete();

    }

    public void accessEntities() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        // create a class from IRI
        IRI iri = IRI.create("http://www.semanticweb.org/owlapi/ontologies/ontology#A");
        OWLClass clsAMethodA = factory.getOWLClass(iri);
        // create a class from prefix manager and specify abbreviated IRIs.
        PrefixManager pm = new DefaultPrefixManager(
                "http://www.semanticweb.org/owlapi/ontologies/ontology#");
        @SuppressWarnings("unused")
        OWLClass clsAMethodB = factory.getOWLClass(":A", pm);
        OWLOntology ontology = manager.createOntology(IRI
                .create("http://www.semanticweb.org/owlapi/ontologies/ontology"));
//        OWLDeclarationAxiom declarationAxiom = factory
//                .getOWLDeclarationAxiom(clsAMethodB);
        OWLDeclarationAxiom declarationAxiom = factory
                .getOWLDeclarationAxiom(clsAMethodA);
        manager.addAxiom(ontology, declarationAxiom);
        System.out.println("Loaded ontology: " + ontology);
    }
    public void buildDataRanges(String path) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLDatatype integer = factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
        OWLDatatype integerDatatype = factory.getIntegerOWLDatatype();
        OWLDatatype floatDatatype = factory.getFloatOWLDatatype();
        OWLDatatype doubleDatatype = factory.getDoubleOWLDatatype();
        OWLDatatype booleanDatatype = factory.getBooleanOWLDatatype();
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded: " + ont.getOntologyID());

        String prefix = ont.getOntologyID().getOntologyIRI() + "#";
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
//        OWLClass person = man.getOWLDataFactory().getOWLClass(
//                IRI.create(prefix + "person"));
        OWLDataFactory fac = man.getOWLDataFactory();
        OWLClass person = fac.getOWLClass(IRI.create(prefix + "person"));
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(person, true);
        Set<OWLClass> clses = subClses.getFlattened();
        System.out.println("print ontology: " + clses);
        OWLDatatype rdfsLiteral = factory.getTopDatatype();
        OWLLiteral eighteen = factory.getOWLLiteral(18);
        OWLDatatypeRestriction integerGE18 = factory.getOWLDatatypeRestriction(integer,
                MIN_INCLUSIVE, eighteen);

        PrefixManager pm = new DefaultPrefixManager(
                "http://www.semanticweb.org/ontologies/dataranges#");
        OWLDataProperty hasAge = factory.getOWLDataProperty(":hasAge", pm);
        OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(
                hasAge, integerGE18);
        OWLOntology ontology = manager.createOntology(IRI
                .create("http://www.semanticweb.org/ontologies/dataranges"));
        manager.addAxiom(ontology, rangeAxiom);
        // >= 60
        OWLDatatypeRestriction integerGE60 = factory
                .getOWLDatatypeMinInclusiveRestriction(60);
        // <= 18
        OWLDatatypeRestriction integerLT16 = factory
                .getOWLDatatypeMaxExclusiveRestriction(18);
        // less than 16 or integer greater or equal to 60
        OWLDataUnionOf concessionaryAge = factory.getOWLDataUnionOf(integerLT16,
                integerGE60);
        OWLDatatype concessionaryAgeDatatype = factory.getOWLDatatype(
                ":ConcessionaryAge", pm);
        OWLDatatypeDefinitionAxiom datatypeDef = factory.getOWLDatatypeDefinitionAxiom(
                concessionaryAgeDatatype, concessionaryAge);
        manager.addAxiom(ontology, datatypeDef);
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new ByteArrayOutputStream()));
        System.out.println("print ontology: " + ontology);
    }

    public void useDataRanges() throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String base = "http://org.semanticweb.datarangeexample";
        OWLOntology ont = man.createOntology(IRI.create(base));
        OWLDataFactory factory = man.getOWLDataFactory();
        // create owl data property: hasAge
        OWLDataProperty hasAge = factory.getOWLDataProperty(IRI.create(base + "hasAge"));

        OWLFunctionalDataPropertyAxiom funcAx = factory
                .getOWLFunctionalDataPropertyAxiom(hasAge);
        man.applyChange(new AddAxiom(ont, funcAx));
        // create data range which corresponds to int greater than 18.
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        // create the value '18', which is int
        OWLLiteral eighteenConstant = factory.getOWLLiteral(18);
        // >=
        OWLFacet facet = MIN_INCLUSIVE;
        //define the dataRange >=18
        OWLDataRange intGreaterThan18 = factory.getOWLDatatypeRestriction(intDatatype,
                facet, eighteenConstant);
        // use data range to restrict hasAge
        OWLClassExpression thingsWithAgeGreaterOrEqualTo18 = factory
                .getOWLDataSomeValuesFrom(hasAge, intGreaterThan18);
        // create adult as an ontology class
        OWLClass adult = factory.getOWLClass(IRI.create(base + "#Adult"));

        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(adult,
                thingsWithAgeGreaterOrEqualTo18);
        man.applyChange(new AddAxiom(ont, ax));
        System.out.println("ontology" + ont);
    }

    public void instantiateLiterals(){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        // Get a plain literal with an empty language tag
        OWLLiteral literal1 = factory.getOWLLiteral("My string literal", "");
        // Get an untyped string literal with a language tag
        OWLLiteral literal2 = factory.getOWLLiteral("My string literal", "en");
        // Get a typed literal to represent the integer 33
        OWLDatatype integerDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER
                .getIRI());
        OWLLiteral literal3 = factory.getOWLLiteral("33", integerDatatype);
        // Create a literal to represent the integer 33
        OWLLiteral literal4 = factory.getOWLLiteral(33);
        // Create a literal to represent the double 33.3
        OWLLiteral literal5 = factory.getOWLLiteral(33.3);
        // Create a literal to represent the boolean value true
        OWLLiteral literal6 = factory.getOWLLiteral(true);
    }
    public void addAxiom() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://www.co-ode.org/ontologies/testont.owl");
        IRI documentIRI = IRI.create("file:/tmp/MyOnt.owl");
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
        manager.addIRIMapper(mapper);
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
        OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);
        AddAxiom addAxiom = new AddAxiom(ontology, axiom);
        manager.applyChange(addAxiom);
        for (OWLClass cls : ontology.getClassesInSignature()) {
            System.out.println("Referenced class: " + cls);
        }
        // We should also find that B is an ASSERTED superclass of A
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(clsA, true);
        System.out.println("Asserted superclasses of " + clsA + ":");
        for (Node<OWLClass> desc : superClasses) {
            System.out.println(desc);
        }
        // Now save the ontology. The ontology will be saved to the location
        // where we loaded it from, in the default ontology format
        manager.saveOntology(ontology);
    }

    public void creatOntology() throws OWLOntologyCreationException {
        // ontology and version
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://www.semanticweb.org/ontologies/myontology");
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        System.out.println("Created ontology: " + ontology);
        // print ontology ID
        OWLOntologyID ontologyID = ontology.getOntologyID();
        System.out.println("Ontology IRI: " + ontologyID.getOntologyIRI());
        System.out.println("Ontology Version IRI: " + ontologyID.getVersionIRI());
        System.out.println("Anonymous Ontology: " + ontologyID.isAnonymous());

        IRI versionIRI = IRI.create(ontologyIRI + "/version1");
        OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);
        SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
        manager.applyChange(setOntologyID);
        System.out.println("Ontology: " + ontology);
        IRI ontologyIRI2 = IRI
                .create("http://www.semanticweb.org/ontologies/myontology2");
        IRI versionIRI2 = IRI
                .create("http://www.semanticweb.org/ontologies/myontology2/newversion");
        OWLOntologyID ontologyID2 = new OWLOntologyID(ontologyIRI2, versionIRI2);
        OWLOntology ontology2 = manager.createOntology(ontologyID2);
        System.out.println("Created ontology: " + ontology2);
        System.out.println("Anonymous: " + ontology2.isAnonymous());
        OWLOntology anonOntology = manager.createOntology();
        System.out.println("Created ontology: " + anonOntology);

        // This ontology is anonymous
        System.out.println("Anonymous: " + anonOntology.isAnonymous());
    }

    public void createPropertyAssertations() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://example.com/owl/families/");
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
        OWLNamedIndividual john = factory.getOWLNamedIndividual(":John", pm);
        OWLNamedIndividual mary = factory.getOWLNamedIndividual(":Mary", pm);

        OWLObjectProperty hasWife = factory.getOWLObjectProperty(":hasWife", pm);
        // Object property
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory
                .getOWLObjectPropertyAssertionAxiom(hasWife, john, mary);
        manager.addAxiom(ontology, propertyAssertion);
        OWLDataProperty hasAge = factory.getOWLDataProperty(":hasAge", pm);
        OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
                .getOWLDataPropertyAssertionAxiom(hasAge, john, 51);
        manager.addAxiom(ontology, dataPropertyAssertion);
        // Note that the above is a shortcut for creating a typed literal and
        // specifying this typed literal as the value of the property assertion.
        OWLDatatype integerDatatype = factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER
                .getIRI());
        // define a literal with integer data type
        OWLLiteral literal = factory.getOWLLiteral("51", integerDatatype);
        OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(hasAge, john, literal);
        manager.addAxiom(ontology, ax);
        // Dump the ontology to System.out
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new ByteArrayOutputStream()));
    }

    public void addClassAssertion() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        String base = "http://example.com/owl/families/";
        PrefixManager pm = new DefaultPrefixManager(base);
        OWLClass person = dataFactory.getOWLClass(":Person", pm);
        OWLNamedIndividual mary = dataFactory.getOWLNamedIndividual(":Mary", pm);
        // Mary is an instance of Person
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(
                person, mary);
        OWLOntology ontology = manager.createOntology(IRI.create(base));
        manager.addAxiom(ontology, classAssertion);
        // Dump the ontology to stdout
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new ByteArrayOutputStream()));
    }

    public void createAndSaveOntology() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://www.co-ode.org/ontologies/testont.owl");
        IRI documentIRI = IRI.create("file:/tmp/SWRLTest.owl");
        // create mapper between ontology and document
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
        manager.addIRIMapper(mapper);

        OWLOntology ontology = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();

        OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
        OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));

        SWRLVariable var = factory.getSWRLVariable(IRI.create(ontologyIRI + "#x"));
        SWRLRule rule = factory.getSWRLRule(
                Collections.singleton(factory.getSWRLClassAtom(clsA, var)),
                Collections.singleton(factory.getSWRLClassAtom(clsB, var)));
        manager.applyChange(new AddAxiom(ontology, rule));

        OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                + "#propA"));
        OWLObjectProperty propB = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                + "#propB"));
        SWRLObjectPropertyAtom propAtom = factory.getSWRLObjectPropertyAtom(prop, var,
                var);
        SWRLObjectPropertyAtom propAtom2 = factory.getSWRLObjectPropertyAtom(propB, var,
                var);

        Set<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
        antecedent.add(propAtom);
        antecedent.add(propAtom2);
        SWRLRule rule2 = factory.getSWRLRule(antecedent, Collections.singleton(propAtom));
        manager.applyChange(new AddAxiom(ontology, rule2));
        // Now save the ontology. The ontology will be saved to the location
        // where we loaded it from, in the default ontology format
        manager.saveOntology(ontology);

    }

    public void addObjectPropertyAssertation() throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String base = "http://www.semanticweb.org/ontologies/individualsexample";
        OWLOntology ont = man.createOntology(IRI.create(base));
        OWLDataFactory dataFactory = man.getOWLDataFactory();

        OWLIndividual matthew = dataFactory.getOWLNamedIndividual(IRI.create(base
                + "#matthew"));
        OWLIndividual peter = dataFactory.getOWLNamedIndividual(IRI.create(base
                + "#peter"));
        OWLObjectProperty hasFather = dataFactory.getOWLObjectProperty(IRI.create(base
                + "#hasFather"));
        OWLObjectPropertyAssertionAxiom assertion = dataFactory
                .getOWLObjectPropertyAssertionAxiom(hasFather, matthew, peter);
        AddAxiom addAxiomChange = new AddAxiom(ont, assertion);
        man.applyChange(addAxiomChange);
        // specify an instance
        OWLClass personClass = dataFactory.getOWLClass(IRI.create(base + "#Person"));
        OWLClassAssertionAxiom ax = dataFactory.getOWLClassAssertionAxiom(personClass,
                matthew);
        man.addAxiom(ont, ax);
        man.saveOntology(ont, IRI.create("file:/tmp/example.owl"));
    }

    public void createRestriction() throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String base = "http://org.semanticweb.restrictionexample";
        OWLOntology ont = man.createOntology(IRI.create(base));

        OWLDataFactory factory = man.getOWLDataFactory();
        OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI.create(base
                + "#hasPart"));
        OWLClass nose = factory.getOWLClass(IRI.create(base + "#Nose"));
        OWLClassExpression hasPartSomeNose = factory.getOWLObjectSomeValuesFrom(hasPart,
                nose);
        OWLClass head = factory.getOWLClass(IRI.create(base + "#Head"));
        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(head, hasPartSomeNose);
        // Add the axiom to our ontology
        AddAxiom addAx = new AddAxiom(ont, ax);
        man.applyChange(addAx);
    }

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
                .create("http://protege.stanford.edu/ontologies/groceries/Vitamin"));
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(Almond, true);
        // print the result with flattened set
        Set<OWLClass> clses = subClses.getFlattened();
        System.out.println("Subclasses of Vitamin: ");
        for (OWLClass cls : clses) {
            System.out.println("    " + cls);
        }
        System.out.println("\n");
    }

    private static void print(Node<OWLClass> parent, OWLReasoner reasoner, int depth) {
        // We don't want to print out the bottom node (containing owl:Nothing
        // and unsatisfiable classes) because this would appear as a leaf node
        // everywhere
        if (parent.isBottomNode()) {
            return;
        }
        // Print an indent to denote parent-child relationships
        printIndent(depth);
        // Now print the node (containing the child classes)
        printNode(parent);
        for (Node<OWLClass> child : reasoner.getSubClasses(
                parent.getRepresentativeElement(), true)) {
            // Recurse to do the children. Note that we don't have to worry
            // about cycles as there are non in the inferred class hierarchy
            // graph - a cycle gets collapsed into a single node since each
            // class in the cycle is equivalent.
            print(child, reasoner, depth + 1);
        }
    }

    private static void printIndent(int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("    ");
        }
    }

    private static void printNode(Node<OWLClass> node) {
        DefaultPrefixManager pm = new DefaultPrefixManager(
                "http://owl.man.ac.uk/2005/07/sssw/people#");
        // Print out a node as a list of class names in curly brackets
        System.out.print("{");
        for (Iterator<OWLClass> it = node.getEntities().iterator(); it.hasNext();) {
            OWLClass cls = it.next();
            // User a prefix manager to provide a slightly nicer shorter name
            System.out.print(pm.getShortForm(cls));
            if (it.hasNext()) {
                System.out.print(" ");
            }
        }
        System.out.println("}");
    }

    public void createAndReadAnnotations(String path) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded: " + ont.getOntologyID());

        OWLDataFactory df = man.getOWLDataFactory();
        OWLClass pizzaCls = df.getOWLClass(IRI.create(ont.getOntologyID()
                .getOntologyIRI().toString()
                + "#Vitamin"));
        // add a comment Annotation
        OWLAnnotation commentAnno = df.getOWLAnnotation(df.getRDFSComment(),
                df.getOWLLiteral("A class which represents vitamin", "en"));
        // define the axiom
        OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(pizzaCls.getIRI(), commentAnno);
        man.applyChange(new AddAxiom(ont, ax));
        // add annotation of literal
        OWLLiteral lit = df.getOWLLiteral("Added a comment to the pizza class");
        OWLAnnotation anno = df.getOWLAnnotation(
                df.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI()),
                lit);
        man.applyChange(new AddOntologyAnnotation(ont, anno));

        OWLAnnotationProperty label = df
                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
    }

    public void createInferredAxiom(String path) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded: " + ont.getOntologyID());

        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        gens.add(new InferredSubClassAxiomGenerator());

        OWLOntology infOnt = man.createOntology();
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology((OWLDataFactory) man, infOnt);
        man.saveOntology(infOnt, new StringDocumentTarget());
    }


    public void walkOntologies(String path) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded: " + ont.getOntologyID());

        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(ont));
        OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker){
            @Override
            public void visit(OWLObjectSomeValuesFrom desc) {
                // Print out the restriction
                System.out.println(desc);
                // Print out the axiom where the restriction is used
                System.out.println("         " + getCurrentAxiom());
                System.out.println();
                // We don't need to return anything here.
            }
        };
        walker.walkStructure(visitor);
    }

    public void queryWithReasoner(String path) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded: " + ont.getOntologyID());

        String prefix = ont.getOntologyID().getOntologyIRI() + "#";
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
        OWLClass margheritaPizza = man.getOWLDataFactory().getOWLClass(
                IRI.create(prefix + "Vitamin"));
        printProperties(man, ont, reasoner, margheritaPizza);

        OWLClass mozzarellaTopping = man.getOWLDataFactory().getOWLClass(
                IRI.create(prefix + "VitaminC"));
        OWLObjectProperty hasOrigin = man.getOWLDataFactory().getOWLObjectProperty(
                IRI.create(prefix + "hasCountryOfOrigin"));
        if (hasProperty(man, reasoner, mozzarellaTopping, hasOrigin)) {
            System.out.println("Instances of " + mozzarellaTopping
                    + " have a country of origin");
        }
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
    public void owlPrime(String path) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://example.com/owlapi/families");
        OWLOntology ont = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLIndividual john = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#John"));
        OWLIndividual mary = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#Mary"));
        OWLIndividual susan = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#Susan"));
        OWLIndividual bill = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#Bill"));

        OWLObjectProperty hasWife = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                + "#hasWife"));
        OWLObjectPropertyAssertionAxiom axiom1 = factory
                .getOWLObjectPropertyAssertionAxiom(hasWife, john, mary);

        AddAxiom addAxiom1 = new AddAxiom(ont, axiom1);
        manager.applyChange(addAxiom1);
        OWLObjectProperty hasSon = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                + "#hasSon"));
        OWLAxiom axiom2 = factory.getOWLObjectPropertyAssertionAxiom(hasSon, john, bill);
        manager.applyChange(new AddAxiom(ont, axiom2));

        OWLObjectProperty hasDaughter = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#hasDaughter"));
        OWLAxiom axiom3 = factory.getOWLObjectPropertyAssertionAxiom(hasDaughter, john,
                susan);
        manager.applyChange(new AddAxiom(ont, axiom3));

        OWLDataProperty hasAge = factory.getOWLDataProperty(IRI.create(ontologyIRI
                + "#hasAge"));
        OWLAxiom axiom4 = factory.getOWLDataPropertyAssertionAxiom(hasAge, john, 33);
        manager.applyChange(new AddAxiom(ont, axiom4));
        // manually create the constant
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        OWLLiteral thirtyThree = factory.getOWLLiteral("33", intDatatype);
        factory.getOWLDataPropertyAssertionAxiom(hasAge, john, thirtyThree);
        // add axioms as a set
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasSon, mary, bill));
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(hasDaughter, mary, susan));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasAge, mary, 31));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasAge, bill, 13));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasAge, mary, 8));
        manager.addAxioms(ont, axioms);

        OWLClass person = factory.getOWLClass(IRI.create(ontologyIRI + "#Person"));
        Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
        domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(hasWife, person));
        domainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(hasWife, person));
        domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(hasSon, person));
        domainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(hasSon, person));
        domainsAndRanges
                .add(factory.getOWLObjectPropertyDomainAxiom(hasDaughter, person));
        domainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(hasDaughter, person));
        domainsAndRanges.add(factory.getOWLDataPropertyDomainAxiom(hasAge, person));
        OWLDatatype integerDatatype = factory.getIntegerOWLDatatype();
        domainsAndRanges.add(factory
                .getOWLDataPropertyRangeAxiom(hasAge, integerDatatype));
        manager.addAxioms(ont, domainsAndRanges);
        OWLClassAssertionAxiom classAssertionAx = factory.getOWLClassAssertionAxiom(
                person, john);
        manager.addAxiom(ont, classAssertionAx);

        OWLObjectProperty hasHusband = factory.getOWLObjectProperty(IRI.create(ont
                .getOntologyID().getOntologyIRI() + "#hasHusband"));
        manager.addAxiom(ont,
                factory.getOWLInverseObjectPropertiesAxiom(hasWife, hasHusband));

        OWLObjectProperty hasChild = factory.getOWLObjectProperty(IRI.create(ont
                .getOntologyID().getOntologyIRI() + "#hasChild"));
        OWLSubObjectPropertyOfAxiom hasSonSubHasChildAx = factory
                .getOWLSubObjectPropertyOfAxiom(hasSon, hasChild);

        manager.addAxiom(ont, hasSonSubHasChildAx);
        // And hasDaughter, which is also a sub property of hasChild
        manager.addAxiom(ont,
                factory.getOWLSubObjectPropertyOfAxiom(hasDaughter, hasChild));

        Set<OWLAxiom> hasWifeAxioms = new HashSet<OWLAxiom>();
        hasWifeAxioms.add(factory.getOWLFunctionalObjectPropertyAxiom(hasWife));
        hasWifeAxioms.add(factory.getOWLInverseFunctionalObjectPropertyAxiom(hasWife));
        hasWifeAxioms.add(factory.getOWLIrreflexiveObjectPropertyAxiom(hasWife));
        hasWifeAxioms.add(factory.getOWLAsymmetricObjectPropertyAxiom(hasWife));

        manager.addAxioms(ont, hasWifeAxioms);

        OWLClass man = factory.getOWLClass(IRI.create(ontologyIRI + "#Man"));
        OWLClass woman = factory.getOWLClass(IRI.create(ontologyIRI + "#Woman"));
        OWLClass parent = factory.getOWLClass(IRI.create(ontologyIRI + "#Parent"));

        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(man, person));
        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(woman, person));
        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(parent, person));

        OWLDataExactCardinality hasAgeRestriction = factory.getOWLDataExactCardinality(1,
                hasAge);

        OWLIndividual male = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#male"));
        OWLIndividual female = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
                + "#female"));
        OWLObjectProperty hasGender = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                + "#hasGender"));
        Set<OWLAxiom> genders = new HashSet<OWLAxiom>();
        genders.add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, john, male));
        genders.add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, mary, female));
        genders.add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, bill, male));
        genders.add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, susan, female));
        // Add the facts about the genders
        manager.addAxioms(ont, genders);

        OWLObjectExactCardinality hasGenderRestriction = factory
                .getOWLObjectExactCardinality(1, hasGender);
        OWLObjectOneOf maleOrFemale = factory.getOWLObjectOneOf(male, female);
        OWLObjectAllValuesFrom hasGenderOnlyMaleFemale = factory
                .getOWLObjectAllValuesFrom(hasGender, maleOrFemale);

        OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(
                hasAgeRestriction, hasGenderRestriction, hasGenderOnlyMaleFemale);

        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(person, intersection));
        OWLObjectHasValue hasGenderValueMaleRestriction = factory.getOWLObjectHasValue(
                hasGender, male);
        OWLClassExpression personAndHasGenderValueMale = factory
                .getOWLObjectIntersectionOf(person, hasGenderValueMaleRestriction);

        manager.addAxiom(ont, factory.getOWLObjectPropertyRangeAxiom(hasSon,
                personAndHasGenderValueMale));
        OWLClassExpression rangeOfHasDaughter = factory.getOWLObjectIntersectionOf(
                person, factory.getOWLObjectHasValue(hasGender, female));
        manager.addAxiom(ont,
                factory.getOWLObjectPropertyRangeAxiom(hasDaughter, rangeOfHasDaughter));

        // 13<=age<=20
        OWLFacetRestriction geq13 = factory.getOWLFacetRestriction(MIN_INCLUSIVE,
                factory.getOWLLiteral(13));
        OWLFacetRestriction lt20 = factory.getOWLFacetRestriction(MAX_EXCLUSIVE, 20);
        OWLDataRange dataRng = factory.getOWLDatatypeRestriction(integerDatatype, geq13,
                lt20);

        OWLDataSomeValuesFrom teenagerAgeRestriction = factory.getOWLDataSomeValuesFrom(
                hasAge, dataRng);
        OWLClassExpression teenagePerson = factory.getOWLObjectIntersectionOf(person,
                teenagerAgeRestriction);

        OWLClass teenager = factory.getOWLClass(IRI.create(ontologyIRI + "#Teenager"));
        OWLEquivalentClassesAxiom teenagerDefinition = factory
                .getOWLEquivalentClassesAxiom(teenager, teenagePerson);
        manager.addAxiom(ont, teenagerDefinition);

        OWLDataRange geq21 = factory.getOWLDatatypeRestriction(integerDatatype,
                factory.getOWLFacetRestriction(MIN_INCLUSIVE, 21));
        OWLClass adult = factory.getOWLClass(IRI.create(ontologyIRI + "#Adult"));
        OWLClassExpression adultAgeRestriction = factory.getOWLDataSomeValuesFrom(hasAge,
                geq21);
        OWLClassExpression adultPerson = factory.getOWLObjectIntersectionOf(person,
                adultAgeRestriction);
        OWLAxiom adultDefinition = factory.getOWLEquivalentClassesAxiom(adult,
                adultPerson);
        manager.addAxiom(ont, adultDefinition);

        OWLDataRange notGeq21 = factory.getOWLDataComplementOf(geq21);
        OWLClass child = factory.getOWLClass(IRI.create(ontologyIRI + "#Child"));
        OWLClassExpression childAgeRestriction = factory.getOWLDataSomeValuesFrom(hasAge,
                notGeq21);
        OWLClassExpression childPerson = factory.getOWLObjectIntersectionOf(person,
                childAgeRestriction);
        OWLAxiom childDefinition = factory.getOWLEquivalentClassesAxiom(child,
                childPerson);
        manager.addAxiom(ont, childDefinition);

        // different individuals Axiom
        OWLDifferentIndividualsAxiom diffInds = factory.getOWLDifferentIndividualsAxiom(
                john, mary, bill, susan);
        manager.addAxiom(ont, diffInds);
        manager.addAxiom(ont, factory.getOWLDifferentIndividualsAxiom(male, female));
        OWLDisjointClassesAxiom disjointClassesAxiom = factory
                .getOWLDisjointClassesAxiom(man, woman);

        manager.addAxiom(ont, disjointClassesAxiom);
        System.out.println("RDF/XML: ");
        manager.saveOntology(ont, new StreamDocumentTarget(System.out));
        // OWL/XML
        System.out.println("OWL/XML: ");
        manager.saveOntology(ont, new OWLXMLOntologyFormat(), new StreamDocumentTarget(
                System.out));
        // Manchester Syntax
        System.out.println("Manchester syntax: ");
        manager.saveOntology(ont, new ManchesterOWLSyntaxOntologyFormat(),
                new StreamDocumentTarget(System.out));
        // Turtle
        System.out.println("Turtle: ");
        manager.saveOntology(ont, new TurtleOntologyFormat(), new StreamDocumentTarget(
                System.out));
    }
    private static OWLReasoner createReasoner(final OWLOntology rootOntology) {
        // We need to create an instance of OWLReasoner. An OWLReasoner provides
        // the basic query functionality that we need, for example the ability
        // obtain the subclasses of a class etc. To do this we use a reasoner
        // factory.
        // Create a reasoner factory.
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        return reasonerFactory.createReasoner(rootOntology);
    }
}

public class demo {

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        String path = "/javaProject/hermit/posc-groceries.owl";
        Ontology ontology = new Ontology();
//        ontology.saveOntology(path);
//        ontology.accessEntities();
//        ontology.buildDataRanges();
        ontology.useReasoner(path);
    }

}
