**WikiCorpusGenerator**

With this class we generate corpus for further training on neural networks. Essentially, it gets a list of wikipedia articles and transforms them into an annotated corpus of documents, where each document corresponds to a wikipedia article. The process consists of three steps: 
1. Collecting the entities from which wiki corpus documents will be generated
2. Transforming the text of each article into a corpus document
    1. computing labels for each entity mention
        * retrieving the text by the link found in DB
        * extracting the first paragraph for the corpus document
        * collecting all the outgoing entities and their child entities
        * keeping only \<threshold\> of all entities sorted by Milne similarity with the article entity. If \<threshold\> = 1.0 then we consider all the entities
        * computing `NerType.Label` for each mention of each considered entity (if a mention appears in many entities, then the mention gets the label from the entity with the highest Milne similarity)
            * we filter out those mentions which
                * contain "<span", "=", "px", "<!--"
                * contain "(" and ")"
                * is a date
                * contain some of the symbols: .,\/#!$%\^&\*;:{}=\-_`~()
                * end with "'s"
            * if mention is in _knownCountries_ then it is `NerType.Label.LOCATION`
            * if mention is in _languagesList_ then it is `NerType.Label.MISC`
                
    2. annotating tokens in each sentence
        * spotting all the mentions in the sentence by comparing mentions from AIDA and conflated sentence
        * filtering out spots which contain only digits or start with lowercase letter (method `filterSpots()`)
        * filtering out spots which overlap (method `getNonOverlappingSpots()`). First we sort them such that the first spots come first. If the spots have the same starting position than the longer spot comes first. If the spots overlap then we break the tie by the following rules:
            * if the previous spot is shorter than the next one, the next one is selected
            * if the next spot is in form of "City, Country", the next one is selected
        * annotating the sentence with the spots
3. Assembling the corpus

