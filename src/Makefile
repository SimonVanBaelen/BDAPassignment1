##
## Makefile
##
## Pieter Robberechts
## June 2019
## 

# Experiment parameters ######################################################

SMALL_DATAFOLDER=..

SMALL_DATA_PC=${SMALL_DATAFOLDER}/dataSubset/pc/
SMALL_DATA_VFDT=${SMALL_DATAFOLDER}/dataSubset/vfdt/
NB_FEATURE_VALUES_SMALL=${SMALL_DATAFOLDER}/dataSubset/nbFeatureValues.csv

DATAFOLDER=/cw/bdap/assignment1/data

DATA_PC_CLEAN=${DATAFOLDER}/clean/PC/
DATA_PC_NOISE=${DATAFOLDER}/noisy/PC/
DATA_VFDT_CLEAN=${DATAFOLDER}/clean/VFDT/
DATA_VFDT_NOISE=${DATAFOLDER}/noisy/VFDT/

NB_FEATURE_VALUES=${DATAFOLDER}/nbFeatureValues.csv


# Parameters for perceptron --> 0.00001 vele beter 0.000000001
ETA=0.000000001

# Parameters for VFDT
DELTA=0.1
TAU=0.05
NMIN=200

# Experiment parameters
OUT=out
PERIOD=100000  # Original was 200000

# Compilation  ###############################################################

## Locate directories
class_d=bin
lib_d=lib
source_d=src

# Compilation stuff
JAVAC=javac
JFLAGS=-g -d $(class_d) -sourcepath $(source_d) -cp .:$(lib_d)/* -Xlint:all

clean:
	rm -rf $(class_d)/*

$(class_d)/Example.class: $(source_d)/Example.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Data.class: $(source_d)/Data.java $(class_d)/Example.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/IncrementalLearner.class: $(source_d)/IncrementalLearner.java $(class_d)/Data.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Perceptron.class: $(source_d)/Perceptron.java $(class_d)/Data.class $(class_d)/Example.class $(class_d)/IncrementalLearner.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/VfdtNode.class: $(source_d)/VfdtNode.java
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/Vfdt.class: $(source_d)/Vfdt.java $(class_d)/VfdtNode.class $(class_d)/Data.class $(class_d)/Example.class $(class_d)/IncrementalLearner.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/PerceptronSanityChecks.class: $(source_d)/PerceptronSanityChecks.java $(class_d)/Perceptron.class
	@$(JAVAC) $(JFLAGS) $<

$(class_d)/VfdtSanityChecks.class: $(source_d)/VfdtSanityChecks.java $(class_d)/Vfdt.class
	@$(JAVAC) $(JFLAGS) $<

# Sanity checks ##############################################################

check_pc: $(class_d)/PerceptronSanityChecks.class
	@echo "Checking perceptron with sanity tests";
	java -cp .:$(class_d):$(lib_d)/* org.junit.runner.JUnitCore PerceptronSanityChecks

check_vfdt: $(class_d)/VfdtSanityChecks.class
	@echo "Checking vfdt with sanity tests";
	java -cp .:$(class_d):$(lib_d)/* org.junit.runner.JUnitCore VfdtSanityChecks

# Experiments ################################################################

pc_small: $(class_d)/Perceptron.class
	@echo "Testing perceptron on subset of the clean data"
	rm -f small.pc.acc ;
	rm -f small.pc.prob;
	time java -cp .:$(class_d) Perceptron $(ETA) $(SMALL_DATA_PC) small 1000 -writeOutAllPredictions

pc_clean: $(class_d)/Perceptron.class
	@echo "training perceptron on all data"
	rm -f $(OUT).clean.pc.acc ;
	rm -f $(OUT).clean.pc.prob;
	time java -Xmx1800m -cp .:$(class_d) Perceptron $(ETA) $(DATA_PC_CLEAN) $(OUT).clean $(PERIOD)

pc_noise: $(class_d)/Perceptron.class
	@echo "training perceptron on all data"
	rm -f $(OUT).noise.pc.acc ;
	rm -f $(OUT).noise.pc.prob;
	time java -Xmx1800m -cp .:$(class_d) Perceptron $(ETA) $(DATA_PC_NOISE) $(OUT).noise $(PERIOD)

vfdt_small: $(class_d)/Vfdt.class
	@echo "Testing VFDT on subset of clean data"
	rm -f small.vfdt.acc ;
	rm -f small.vfdt.prob;
	time java -cp .:$(class_d) Vfdt $(DELTA) $(TAU) $(NMIN) $(SMALL_DATA_VFDT) $(NB_FEATURE_VALUES_SMALL) small 1000 -writeOutAllPredictions

vfdt_clean: $(class_d)/Vfdt.class
	@echo "Testing VFDT on subset of data"
	rm -f $(OUT).clean.vfdt.acc ;
	rm -f $(OUT).clean.vfdt.prob;
	time java -Xmx1800m -cp .:$(class_d) Vfdt $(DELTA) $(TAU) $(NMIN) $(DATA_VFDT_CLEAN) $(NB_FEATURE_VALUES) $(OUT).clean $(PERIOD)

vfdt_noise: $(class_d)/Vfdt.class
	@echo "Testing VFDT on subset of data"
	rm -f $(OUT).noise.vfdt.acc ;
	rm -f $(OUT).noise.vfdt.prob;
	time java -Xmx1800m -cp .:$(class_d) Vfdt $(DELTA) $(TAU) $(NMIN) $(DATA_VFDT_NOISE) $(NB_FEATURE_VALUES) $(OUT).noise $(PERIOD)
