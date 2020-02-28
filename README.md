# Oma lisäys:
Tämä projekti on Turun Yliopiston kurssin (Hajautetut ohjelmistot ja pilvipalvelut) harjoitustyö. Meille annettiin valmiina Javalla koodattu peli, jossa gorillat heittelevät toisiaan banaaneilla. Käyttöliittymä on toteutettu JavaFX:llä. Alkuperäistä versiota pystyi pelaamaan vain yksin tietokonetta vastaan. Tehtävänä oli laajentaa peli moninpeliksi. Oma koodini on lähinnä luokissa GorillaMultiplayerLogic ja Mesh.

Ensimmäiseksi piti toteuttaa Mesh-verkko, jonka yli pelaajat voivat lähettää toisilleen viestejä. Toisessa vaiheessa piti muokata peliä siten, että pelin pelaaminen onnistuu moninpelinä Mesh-verkon yli. Mesh-verkon idea on, että pelaaja yhdistää koneensa jonkun toisen pelaajan koneeseen, joka on jo osa Mesh-verkkoa. Tämän lisäksi pelaaja toimii palvelimena uusille pelaajille eli Mesh verkosta muodostuu puumainen rakenne. Jokainen toimii siis sekä asiakkaana että palvelimena (pelin käynnistäjä eli puun juuri on tietenkin vain palvelimen roolissa).

Jokaisella pelaajalla täytyy siis olla tämä Java-ohjelma koneellaan. Peli pystytetään siten, että GorillaLogic luokan initialize() metodi kutsuu metodia startServer(), jolle annetaan parametrina "portForClients" niminen komentoriviparametri (default-arvona 1234). Sen jälkeen kutsutaan metodia connectToServer(), jolle annetaan parametrina "address" niminen komentoriviparametri (default-arvona localhost) ja "portForConnection" niminen komentoriviparametri (default-arvona 1234). Luokalla GorillaMultiplayerLogic on muuttuja NUMBER_OF_PLAYERS, joka määrittelee kuinka monta pelaajaa peliin mahtuu. Kun tämä luku on tullut täyteen, peli käynnistetään.

# Original README:
This README contains only general instructions on how to run the game. The development setup, assignment, program structure and hints for completing the assignment can be found in [doc/index.md](doc/index.md) (in Finnish).

# Project description
A game where a bunch of gorillas try to kill each other by throwing bananas on time-limited turns. Last survivor is the winner.

Requires Java 11 or later. Compatible with
Eclipse, IntelliJ IDEA and VS Code with Java Extension Pack. Minor issues with Netbeans.

## Installation

Maven:

```bash
$ git clone https://gitlab.utu.fi/tech/education/distributed-systems/distributed-gorilla

$ cd distributed-gorilla

$ mvn compile package exec:java
```

SBT:

```bash
$ git clone https://gitlab.utu.fi/tech/education/distributed-systems/distributed-gorilla

$ cd distributed-gorilla

$ sbt compile run
```

## JavaFX bugs

JavaFX has serious memory leaks that might lead to a crash in just seconds.
Use the following JVM parameter to switch to software rendering pipeline that
does not have the leaks
```
-Dprism.order=sw
```

E.g.

```bash
$ java -Dprism.order=sw -jar target/distributed-gorilla-1.0.jar
```

The game will allocate significant amounts of memory. Use the following switch
to restrict the heap size to avoid wasting RAM:

```
-Xmx2000m
```

References:

* https://bugs.openjdk.java.net/browse/JDK-8092801
* https://bugs.openjdk.java.net/browse/JDK-8088396
* https://bugs.openjdk.java.net/browse/JDK-8161997
* https://bugs.openjdk.java.net/browse/JDK-8156051
* https://bugs.openjdk.java.net/browse/JDK-8161914
* https://bugs.openjdk.java.net/browse/JDK-8188094
* https://stackoverflow.com/a/41398214

## Further instructions

  * Java platform: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/jvm-platform
  * Maven: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/maven-misc
  * SBT: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/sbt-misc
  * OOMkit: https://gitlab.utu.fi/tech/education/oom/oomkit

Course related

  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-chat
  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-crypto
  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-classloader

## Screenshots

![Game](web/screenshot1.png)
