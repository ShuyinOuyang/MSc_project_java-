import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

class Utility{
    public String readJsonFile(String filepath) throws IOException {
//        String filepath = "/pythonProject/Msc Project/main/ontology.json";
        String jsonStr = "";
        File file = new File(filepath);
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file),"utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read())!=-1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        jsonStr = sb.toString();
        return jsonStr;
    }

    public void createontology(String filepath) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        // read from json file
        String s = readJsonFile(filepath);
        JSONObject jobj = JSON.parseObject(s);
        // get class
        JSONArray classes = jobj.getJSONArray("class");
        // get individual
        JSONArray individuals = jobj.getJSONArray("individual");
        // get object property
        JSONArray objectProperties = jobj.getJSONArray("objectProperty");
        // get data property
        JSONArray dataProperties = jobj.getJSONArray("dataProperty");
//        System.out.println(individuals.get(0));

        // set up ontology utilities
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://example.com/owlapi/healthdata");
        OWLOntology ont = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();

        // local copy of ontology
        File file = new File("/javaProject/hermit/health-data1.owl");
        manager.saveOntology(ont, IRI.create(file.toURI()));
        OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
        manager.saveOntology(ont, owlxmlFormat, IRI.create(file.toURI()));
        //create classes
        for(Object cls:classes){
            String cls_str = String.valueOf(cls);
            OWLClass owlclass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + cls_str));
        }
        // create object property
        List objectProperties_list = new ArrayList();
        for (Object objectProperty: objectProperties){
            JSONObject JsonobjectProperty = (JSONObject) JSON.toJSON(objectProperty);
            String objectProperty_name = (String) JsonobjectProperty.get("name");
            String objectProperty_domain = (String) JsonobjectProperty.get("domain");
            String objectProperty_range = (String) JsonobjectProperty.get("range");
            OWLObjectProperty owlObjectProperty = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                    + "#" + objectProperty_name));
            objectProperties_list.add(objectProperty_name);
            // domain and range
            Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
            OWLClass objectProperty_domain_class = factory.getOWLClass(IRI.create(ontologyIRI + "#" + objectProperty_domain));
            OWLClass objectProperty_range_class = factory.getOWLClass(IRI.create(ontologyIRI + "#" + objectProperty_range));
            domainsAndRanges.add(factory.getOWLObjectPropertyDomainAxiom(owlObjectProperty, objectProperty_domain_class));
            domainsAndRanges.add(factory.getOWLObjectPropertyRangeAxiom(owlObjectProperty, objectProperty_range_class));
            // we should also define the inverse object properties
            OWLObjectProperty owlObjectProperty_inverse = factory.getOWLObjectProperty(IRI.create(ontologyIRI
                    + "#" + objectProperty_name + "_inverse"));
            domainsAndRanges.add(factory.getOWLInverseObjectPropertiesAxiom(owlObjectProperty, owlObjectProperty_inverse));
            manager.addAxioms(ont, domainsAndRanges);
        }
        // create data property
        List dataProperties_list = new ArrayList();
        List dataProperties_type_list = new ArrayList();
        for (Object dataProperty: dataProperties){
            JSONObject JsondataProperty = (JSONObject) JSON.toJSON(dataProperty);
            String dataProperty_name = (String) JsondataProperty.get("name");
            String dataProperty_domain = (String) JsondataProperty.get("domain");
            String dataProperty_range = (String) JsondataProperty.get("range");
            OWLDataProperty owlDataProperty = factory.getOWLDataProperty(IRI.create(ontologyIRI
                    + "#" + dataProperty_name));
            dataProperties_list.add(dataProperty_name);
            dataProperties_type_list.add(dataProperty_range);
            // domain and range
            Set<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
            OWLClass dataProperty_domain_class = factory.getOWLClass(IRI.create(ontologyIRI + "#" + dataProperty_domain));
            domainsAndRanges.add(factory.getOWLDataPropertyDomainAxiom(owlDataProperty, dataProperty_domain_class));
            domainsAndRanges.add(factory.getOWLDataPropertyRangeAxiom(owlDataProperty, OWL2Datatype.valueOf(dataProperty_range)));
            manager.addAxioms(ont, domainsAndRanges);
        }


        // create individuals
        for(Object individual:individuals){
            JSONObject js = (JSONObject) individual;
            Set<String> keys = js.keySet();
            // define the class of the individual
            OWLClass tClass = factory.getOWLClass(IRI.create(ontologyIRI +
                    "#" + (String) js.get("type")));
            OWLNamedIndividual tIndividual = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + String.valueOf(js.get("name"))));
            OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(tClass, tIndividual);
            manager.addAxiom(ont, classAssertion);
        }

        for(Object individual:individuals){

            JSONObject js = (JSONObject) individual;
            Set<String> keys = js.keySet();
            Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
            OWLNamedIndividual tIndividual = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + String.valueOf(js.get("name"))));
            for (String key:keys){
//                System.out.println(js.get(key));
                if (!Objects.equals(key, "name") && !Objects.equals(key, "type")){
                    if (dataProperties_list.contains(key)){
                        OWLDataProperty owlDataProperty = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#" + key));
                        // here we just set the value as string
                        // later we need to modify the value based on data property range
                        String dataProperty_type = (String) dataProperties_type_list.get(dataProperties_list.indexOf(key));
                        OWLDatatype datatype = factory.getOWLDatatype(OWL2Datatype.valueOf(dataProperty_type));

                        OWLLiteral value = factory.getOWLLiteral(String.valueOf(js.get(key)), datatype);
                        axioms.add(factory.getOWLDataPropertyAssertionAxiom(owlDataProperty, tIndividual, value));

                    } else if (objectProperties_list.contains(key)) {
                        OWLObjectProperty owlObjectProperty = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + key));
                        OWLNamedIndividual rIndividual = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + String.valueOf(js.get(key))));
                        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(owlObjectProperty, tIndividual, rIndividual));

                        // inverse object property
                        OWLObjectProperty owlObjectProperty_inverse = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + key+"_inverse"));
                        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(owlObjectProperty_inverse, rIndividual, tIndividual));
                    }
                }
                manager.addAxioms(ont, axioms);
            }
            manager.saveOntology(ont, owlxmlFormat, IRI.create(file.toURI()));
        }
    }
}

public class generateOntology {
    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException {
        String filepath = "/pythonProject/Msc Project/main/ontology.json";
        Utility utility = new Utility();
        utility.createontology(filepath);
//        System.out.println(cls);
    }
}
