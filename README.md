# Javanaise
Projet Système réparti

## Fonctionnalités implémentées : 
- Javanaise-V1
- Javanaise-V2 (Proxy)
- Panne client-serveur

Le coordinateur peut être un peu long à lancer, dû a la gestion des pannes (tentative de reconnection aux clients).
Si vous ne souhaitez pas avoir de gestion de panne, dans le fichier CoordMainImpl, mettez la variable "debug" à false.
Le démarrage est ainsi instantanné.

Pour les tests, lancez le coordinateur et les fichiers Irc.java ou BurstTest.java.
BurstTest peut prendre un ENTIER en argument (qui n'a pas d'utilité autre que pour l'affichage, mais si le programme est lancé sans arguments, ce n'est pas grave, par contre évitez les chaînes de caractère).

