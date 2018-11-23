**KnowNEREvaluation**

This class evaluates performance of KnowNER models with conlleval script.

The class has the following options:
* -m - path to the model we want to evaluate
* -c - corpuses against which we want to evaluate the model (will be considered their testb part). It is possible to pass several values
* -l - the way how we evaluate the model. There are two options: single and multi, both can be passed. With single labelling we transform all the named entity labels in the corpus, predicted and gold, to a single one. With multi labelling the evaluation goes without any transformation of the labels.

The class uses the following configurations in _ner.properties_:
* keyspace_pattern - `KnowNEREvaluation` will use keyspace \<dump\>_\<keyspace_pattern\>_\<language\> for `WikipediaLinkProbability` and `MentionTokenWeights` feature extractors
* lang_resources_location - NerTrainer will use the local resources from \<lang_resources_location\>/\<language\> in annotators and feature extractors