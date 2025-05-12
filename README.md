# expectum

<i>expectum</i> vereinfacht die Verifikation komplexer Testergebnisse in Unittests.

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
verifizieren, was zu erheblichen Testabdeckungslücken führen kann.

### Json basierte Verifikation mit <i>expectum</i>

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
* In die Erwartungsdatei wird für das erwartete <code>"result"</code> eine leere Menge <code>{}</code> eingetragen. 
* Ein initialer Testlauf gibt die Differenz zum aktuellen Testergebnis im json Format aus.
* Wenn das Ergebnis in Ordnung ist, kann man es einfach in die json Datei hineinkopieren.

Mit diesem Vorgehen können in kürzester Zeit sehr umfangreiche Verifikationen erstellt werden.

Das Entstehen von Verifikationslücken ist dabei kaum möglich: <br>
Es werden automatisch alle Felder verifiziert. Der Entwickler kann jedoch für den Test nicht relevante Felder ausblenden 
(siehe Beschreibung weiter unten).

Zur die Verifikation mehrzeiliger Feldinhalte (z.B. XML) kann das in solchen Fällen besser lesbare Format hjson genutzt 
werden. 

## Struktur eines dateibasierten Tests

TODO





