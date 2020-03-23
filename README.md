# SketchNB
Repository for the Sketch-Based Naive Bayes algorithms (SketchNB) implemented in MOA.

For more informations about MOA, check out the official website: 
http://moa.cms.waikato.ac.nz

## Citing SketchNB
To cite the SketchNB in a publication, please cite the following paper: 
> Maroua Bahri, Silviu Maniu, Albert Bifet.
> Sketch-Based Naive Bayes Algorithms for Evolving Data Streams. In the IEEE International Conference on Big Data (Big Data), 2018.

## Important source files
The implementation and related codes used in this work are the following: 
* SketchNB.java: the SketchNB that uses CMS to store synopsis from the stream.
* AdaptiveSketchNB.java: the adaptive version of the SketchNB that uses ADWIN to adapt to changes.
* HashingTrickFilter.java: the hashing trick filter to minimise the dimensionality of data.
* HashingTrickFilterBinary.java: the hashing trick filter that produces a binary representation of data instead of a numerical one.

## How to execute it
To test the SketchNB, you can copy and paste the following command in the interface (right click the configuration text edit and select "Enter configuration”).
Sample command: 

`EvaluatePrequential -l (bayes.SketchBased.SketchNB -d 0.01 -e 0.001) -s (ArffFileStream -f /pathto/tweet1000.arff) -e BasicClassificationPerformanceEvaluator`

Explanation: this command executes CS-kNN prequential evaluation precising the output and input dimensionality, d and f respectively on the tweet500 dataset (-f tweet1.arff). 
**Make sure to extract the tweet1000.arff dataset, and setting -f to its location (pathto), before executing the command.**

## Datasets used in the original paper
The real datasets are compressed and available at the root directory. 
