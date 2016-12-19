KMEANS IMPLEMENTATION FOR HADOOP
=========
kmeans hadoop project.

### General info
* **hadoop: ** 2.3.7
* **java version: ** "1.8.0_111"
* **Java(TM) SE Runtime Environment: ** (build 1.8.0_111-b14)
* **Java HotSpot(TM) 64-Bit Server VM: ** (build 25.111-b14, mixed mode)

###USAGE:
Clone this repo, then:

1) Modify KMeans class and Mapper class and set your s3 bucket path there.

2) Add to the env variables HADOOD_CLASSPATH:
 check and copy the path with 
```{r, engine='bash', count_lines}
$ bin/hadoop classpath
```
3) Add that path to HADOOP_CLASSPATH
```{r, engine='bash', count_lines}
$ export HADOOP_CLASSPATH=/path/to/hadoop/class
```
4) Modify and the build the project
```{r, engine='bash', count_lines}
$javac -classpath ${HADOOP_CLASSPATH} -d ./build *.java
```
5) Change directory to the build folder and prepare the jars
```{r, engine='bash', count_lines}
$  build/ 
$ jar cvf MapRedKMeans.jar *
```
6) Add jar to your EMR.


