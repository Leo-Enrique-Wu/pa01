# Predictive Analysis Summer 2021
## Programming assignment 01
### Instructions:
#### Preprocess:
1. Install maven: Follow the instructions provided by Apache Maven Project(https://maven.apache.org/install.html).
1. Create/update data.txt under the path: /pa01/analyze/input/
* Each line represents a property.
* Format: `<property name>` = `<property's value>`
* The properties and their meanings as following:
    * INPUT: The path of the folder which contains all the target documents that need to be clustered.
        * EX: INPUT=../pa01/analyze/src
    * OUTPUT: The path of the folder where the program put the result.
        * EX: OUTPUT=../pa01/analyze/output
    * SIM_CAL_TYPE: Choose the method to calculate the similarity.
        * E: Euclidean distance
        * C: Cosine similarity
3. Put the documents which we want to cluster under the path we provide as `INPUT`. Under the folder, each cluster has a folder and all the documents belong to the cluster are put under that folder. 
        Assumes article01, article02, article07 belong to the same cluster, C1; article03, article08 belong to C2; article04, article05 belong to C3
        Then,
        /pa01/analyze/src
            ------------------/C1
                             ---/article01.txt
                             ---/...
            ------------------/C2
                             ---/article03.txt
                             ---/article08.txt
            ------------------/C3
                             ---/...
        

#### Build and execute:
1. Go to the root of this project: /pa01
1. Execute the command to build and execute the project: `mvn clean package exec:java`

#### Output:
* The results under the output folder
    * ./docTermMatrix.csv: The document-term matrix.
    * ./topic.csv: The top five topics for each document folder. Each row represent one folder. The first column is the name of the folder, and the following five columns are the topics.
    * ./perfEval.csv: It has the confusion matrix and the precision, recall and F1-score for each folder.
* Visualize the clusters: In the end of the program, it will generate two charts to visualize the clusters on the screen. One is for the output clusters, and the other one is for the original documents clusters given in the dataset. Each chart is in one window, and the title of the window shows that it belongs to the result of the output clusters or the original documents clusters.
    * !!! Be aware that the two charts could be rendered on the same position. You need to move the chart showed on the top to see the other one.