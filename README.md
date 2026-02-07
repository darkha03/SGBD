# SGBD

## Introduction
Ce depot contient un mini systeme de gestion de base de donnees (SGBD) ecrit en Java.
Il sert de support pedagogique pour illustrer les concepts fondamentaux d'un moteur
relationnel, ainsi que la difference entre stockage disque et traitement en memoire.

## Stack
- Langage : Java
- Execution : JVM (Java SE)
- Conception : operateurs relationnels, index de hachage, plans d'execution

## Features
- Stockage par blocs disque et abstractions de lecture
- Tables en memoire et sur disque
- Parcours complets (full scan) en memoire et sur disque
- Filtres d'egalite
- Index de hachage statique et dynamique
- Jointures (sur index, tri-fusion)
- Operateurs d'aggregation (min, avg)
- Optimiseur simple et plan d'execution
- Instrumentation pour mesurer les couts d'execution
