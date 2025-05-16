# expectum

<i>expectum</i> vereinfacht die Verifikation komplexer Testergebnisse in Unittests.

Die Verifikation von dutzenden oder hunderten Einzelwerten erfordert in klassischen jUnit Tests auch
dutzende oder hunderte Java Verifikationscode Zeilen.

<i>expectum</i> ermöglicht die Implementierung solch umfangreiche Verifikationen oft mit nur einer einzigen Java Codezeile.

## Kurze Einführung

### Klassische (manuelle) Testdatenverifikation

Auf klassischem Wege werden Ergebnisobjekte Feld für Feld mit vielen Zeilen Code verglichen:

    void someOperationTest() {
        var result = someOperation();
        
        assertThat(result.getName()).isEqualTo("expectedName");
        assertThat(result.getList()).hasSize(5);
        assertThat(result.getList().get(0)).isEqualTo(0);
        assertThat(result.getList().get(1)).isEqualTo(1);
        ...
    }

Dieses Vorgehen kann bei umfangreichen Ergebnisstrukturen zu sehr aufwändigem Verifikationscode führen.
Die Kosten zur Herstellung und Pflege solcher Verifikationen sind nicht gering.

Oft ist deshalb bei klassischen Verifikationen zu beobachten, dass Entwickler nur eine kleine Teilmenge des Ergebnisses
prüfen, was zu erheblichen Testabdeckungslücken führen kann.

### Textbasierte Verifikation mit <i>expectum</i>

Mit <i>expectum</i> kann die Verifikationen aller Feldwerte eines Ergebnisses mit nur einer Zeile Java Code geprüft werden:

    void someOperationTest() {
        var result = someOperation();

        assertJsonNode(result, "result");
    }

Die zu vergleichenden Erwartungswerte liegen in einer json Datei vor:

    {
        "someOperationTest": {
            “result": {
                "name": "expectedName",
                "list": [0, 1, 2, 3, 4, 5]
            }
        }
    }

Die Herstellung der json Erwartungswerte wird durch <i>expectum</i> unterstützt: 
* In die Erwartungsdatei wird für das erwartete <code>"result"</code> zunächst eine leere Menge <code>{}</code> eingetragen. 
* Ein initialer Testlauf gibt die Differenz zum aktuellen Testergebnis im json Format aus.
* Wenn das Ergebnis in Ordnung ist, kann es einfach in die json Datei hineinkopiert werden.

Mit diesem Vorgehen können in kürzester Zeit sehr umfangreiche Verifikationen erstellt werden.

Das Entstehen von Verifikationslücken ist dabei kaum möglich: <br>
Es werden automatisch alle Felder verifiziert. Der Entwickler kann jedoch für den Test nicht relevante Felder ausblenden 
(siehe Beschreibung weiter unten).

Zur die Verifikation mehrzeiliger Feldinhalte (z.B. XML) kann das in solchen Fällen besser lesbare Format hjson genutzt 
werden. 

## Textdateibasierte Tests

<i>expectum</i> kann Testerwartungsdaten in Dateien, die parallel zur Java Datei der Testklasse vorliegen benutzen.

Angenommen, wir hätten eine Testklasse in <code>/mypackage/MyTest.java</code> vorliegen, dann können die entsprechenden
Testerwartungsdaten in der Datei <code>/mypackage/MyTest.json</code> bereitgestellt werden.<br>
Im Fall von hJson Testdaten wird die Dateiendung <code>.hjson</code> genutzt.

Für jede Testmethode, die Erwartungsdaten in der Json Datei nutzt, wird in der Json Datei ein Element mit dem 
Namen der Methode angelegt.
Darin können weitere Unterelemente für die in der Testmethode zu verifizierenden Objekte angelegt werden.

Die folgenden Kapitel beschreiben die Implementierung solcher Tests.


### Json Tests

Die Nutzung der Basisklasse <code>JsonResourceTest</code> ermöglicht die Nutzung von json basierten Verifikationen.
    
*Wenn dies nicht möglich oder erwünscht ist, kann <i>expectum</i> auch durch Komposition oder Mixin-Interface Vererbung 
bereitgestellt werden. Diese Varianten werden separat beschrieben.*

Die Klasse JsonResourceTest stellt Hilfsmethoden zur Json basierten Verifikation bereit:
* [assertJsonNode(object, jsonPtr)](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/main/java/de/cleanitworks/expectum/core/resource/JsonResourceTestDelegate.java#L179)
  verifiziert die Json Representation des gegeben Objektes gegen das durch das <code>jsonPtr</code> referenzierte Json Element.
* [jsonShow(class, fieldNames)](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/main/java/de/cleanitworks/expectum/core/resource/JsonResourceTestDelegate.java#L212)
  steuert die Json Konvertierung so, dass für die gegebene Klasse nur die angegebenen Felder in Json ausgegeben werden.
* [jsonHide(class, fieldNames)](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/main/java/de/cleanitworks/expectum/core/resource/JsonResourceTestDelegate.java#L201)
  steuert die Json Konvertierung so, dass für die gegebene Klasse alle ausser die angegebenen Felder in Json ausgegeben werden.
* [toJson(object)](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/main/java/de/cleanitworks/expectum/core/resource/JsonResourceTestDelegate.java#L98)
  stellt einen Json String aus dem gegebenen Objekt her.
* [json(jsonPtr)](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/main/java/de/cleanitworks/expectum/core/resource/JsonResourceTestDelegate.java#L140)
  liest ein Element aus der Json Datei des Tests.

In dem folgenden Beispieltest werden diese Operationen genutzt.

* [Java Code](https://github.com/olaf-boede/expectum/blob/bbd8193e13c4a6b69b88a702fd616c05c252afe9/expectum-core/src/test/java/de/cleanitworks/expectum/core/resource/example/MeadowJsonTest.java#L44)<br>
* [Json Erwartungsdaten](https://github.com/olaf-boede/expectum/blob/main/expectum-core/src/test/java/de/cleanitworks/expectum/core/resource/example/MeadowJsonTest.json)

### hJson Tests

[Java Code](https://github.com/olaf-boede/expectum/blob/main/expectum-core/src/test/java/de/cleanitworks/expectum/core/resource/example/MeadowHjsonTest.java)<br>
[hJson Erwartungsdaten](https://github.com/olaf-boede/expectum/blob/main/expectum-core/src/test/java/de/cleanitworks/expectum/core/resource/example/MeadowHjsonTest.hjson)

### Hibernate Tests (unter Berücksichtigung von Lazy Load)

[Java Code](https://github.com/olaf-boede/expectum/blob/main/expectum-hibernate/src/test/java/de/cleanitworks/expectum/hibernate/domaintest/GardenTest.java)<br>
[Json Erwartungsdaten](https://github.com/olaf-boede/expectum/blob/main/expectum-hibernate/src/test/java/de/cleanitworks/expectum/hibernate/domaintest/GardenTest.json)



## TODO:
* Beschreibung von Testvererbung und Testkomposition.
* Ein- und Ausblenden von Feldern und Jackson Nutzung
* Tests von Hibernate Objekten und deren Lazy Load Auflösung
* XML Testung
* Eingehende Erläuterung der Beispiele
* JSONAssert Vergleichsmodi
* Umgang mit veränderlichen Inhalten
* Verweise auf genutzte 3rd Party Projekte





