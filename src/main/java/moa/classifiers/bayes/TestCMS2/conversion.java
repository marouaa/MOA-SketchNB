package moa.classifiers.bayes.TestCMS2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author maroua
 */

 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.Remove;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 public class conversion {
   /**
    * takes an ARFF file as first argument, the number of indices to remove
    * as second and thirdly whether to invert or not (true/false).
    * Dumps the generated data to stdout.
    */
   public static void main(String[] args) throws Exception {
     Instances       inst;
     Instances       instNew;
     Remove          remove;
 
     inst   = new Instances(new BufferedReader(new FileReader("/home/maroua/Desktop/KDDCup99_full.arff")));
     remove = new Remove();
     remove.setAttributeIndices(args[1]);
     remove.setInvertSelection(new Boolean(false).booleanValue());
     remove.setInputFormat(inst);
     instNew = Filter.useFilter(inst, remove);
     System.out.println(instNew);
   }
 }