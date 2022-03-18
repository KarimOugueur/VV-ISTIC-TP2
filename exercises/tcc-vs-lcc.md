# TCC *vs* LCC

Explain under which circumstances *Tight Class Cohesion* (TCC) and *Loose Class Cohesion* (LCC) metrics produce the same value for a given Java class. Build an example of such as class and include the code below or find one example in an open-source project from Github and include the link to the class below. Could LCC be lower than TCC for any given class? Explain.

## Answer

La TCC et LCC portent sur la cohésion d'une classe. Ils se calculent de la manière suivante :
- TCC = NDC / NP ou NDC est le nombre de connexions directes, NP le nombre maximal de connexions possibles
- LCC = (NDC + NIC) / NP ou NDC est le nombre de connexions directes, NIC le nombre de connexion indirectes et NP le nombre maximal de connexions possibles
Ainsi si nous faisons
TCC = LCC --> NDC / NP = (NDC + NIC) / NP --> NDC = NDC + NIC --> NIC = 0
Il faut que le nombre de connexions indirectes soit égale à 0. Ainsi, toutes les connexions sont directes.

Par exemple, cette classe possède une forte cohésion. Elle est facile à maintenir et hautement réutilisable :

    class Multiply {
        int a = 5;
        int b = 5;
        public int mul(int a, int b)
        {
            this.a = a;
            this.b = b;
            return a * b;
        }
    }

Avec le calcul précédent, on peut en déduire que TCC <= LCC. Il y a forcément le même nombre de connexions directes. Il peut également y avoir des connexions indirectes.
