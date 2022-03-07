# Extending PMD

Use XPath to define a new rule for PMD to prevent complex code. The rule should detect the use of three or more nested `if` statements in Java programs so it can detect patterns like the following:

```Java
if (...) {
    ...
    if (...) {
        ...
        if (...) {
            ....
        }
    }

}
```
Notice that the nested `if`s may not be direct children of the outer `if`s. They may be written, for example, inside a `for` loop or any other statement.
Write below the XML definition of your rule.

You can find more information on extending PMD in the following link: https://pmd.github.io/latest/pmd_userdocs_extending_writing_rules_intro.html, as well as help for using `pmd-designer` [here](https://github.com/selabs-ur1/VV-ISTIC-TP2/blob/master/exercises/designer-help.md).

Use your rule with different projects and describe you findings below. See the [instructions](../sujet.md) for suggestions on the projects to use.

## Answer

La commande xpath pour chercher au moins 3 if imbriqués est la commande suivante :

//BlockStatement//IfStatement//IfStatement//IfStatement

La règle xml est la suivante :

<rule name=""
      language="java"
      message=""
      class="net.sourceforge.pmd.lang.rule.XPathRule">
   <description>

   </description>
   <priority>3</priority>
   <properties>
      <property name="version" value="1.0"/>
      <property name="xpath">
         <value>
<![CDATA[
//BlockStatement//IfStatement//IfStatement//IfStatement
]]>
         </value>
      </property>
   </properties>
</rule>

Nous avons testé avec les programmes suivants :

// Doit détecter 3 if imbriqués
public class Main{
   public  static  void main(String[] args) {
        int a = 0, b= 0;
        if (a==b) {
            b=a;
            if (a==b) {
                a=b;
               if (a==b) {
                   a=b;
                }
            }
        }
   }
}        

// Ne doit pas détecter 3 if imbriqués
public class Main{
   public  static  void main(String[] args) {
        int a = 0, b= 0;
        if (a==b) {
            b=a;
            if (a==b) {
                a=b;
            }
            if (a==b) {
                a=b;
            }
        }
   }
}        

Nous avons testé avec plusieurs classes java du projet commons-math. Cette règle a l'air de bien fonctionner et de détecter les if imbriqués.

