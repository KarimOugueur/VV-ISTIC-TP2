# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false negative). Explain why you would not solve this issue.

## Answer

Après le lancement de PMD nous avons trouvés une erreur dans le projet https://github.com/apache/commons-collections de type : 




Problème  toruvé avec PMD, il compare deux objects avec (==) au lieu d'un .equals(). Ce problème est un faux négatif et ne nécissite pas de correction car il veut comparer la référence des objets et pas les valeurs des objets. et si on mets à la place de (==) un .equals, la condition n'aura plus de sense.  
```java
  commons-collections/src/main/java/org/apache/commons/collections4/multiset/AbstractMultiSet.java:77:	CompareObjectsWithEquals:	Use equals() to compare object  references.
``` 
le code Source : 
```java
   /**
     * Returns the number of occurrence of the given element in this multiset by
     * iterating over its entrySet.
     *
     * @param object the object to search for
     * @return the number of occurrences of the object, zero if not found
     */
    @Override
    public int getCount(final Object object) {
        for (final Entry<E> entry : entrySet()) {
            final E element = entry.getElement();
            if (element == object ||
                element != null && element.equals(object)) {
                return entry.getCount();
            }
        }
        return 0;
    }
``` 
