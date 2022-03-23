# Documentation - Coliffimo
![GitHub release (release name instead of tag name)](https://img.shields.io/github/v/release/PeredurOmega/Coliffimo?include_prereleases)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/PeredurOmega/Coliffimo/coliffimo-deployment?label=build-deployment)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/PeredurOmega/Coliffimo/coliffimo-tests?label=build-tests)

Coliffimo est une application de bureau destinée à des services de livraison. 
Elle est utlisée pour calculer le meilleur itinéraire, prenant en compte plusieurs commandes avec chacune un point de retrait et de livraison propres. 


### Table des matières
- Utilisation de Coliffimo
- Spécifications
- Diagramme de classes
- Environnement technique

## Utilisation de Coliffimo
### Lancement de l'application

Vous trouverez dans le projet GitHub les archives zip des différentes versions de l'application. 

Après téléchargement, décompressez l'archive et ouvrez le dossier bin.
Le fichier à exécuter pour lancer l'application est alors Coliffimo.bat (pour Windows) ou Coliffimo (pour Linux).

### Calcul d'un itinéraire

Une fois l'application lancée, vous aurez la possibilité de charger un programme de Pickup&delivery, contenant les informations de la course à effectuer.

Le meilleur itinéraire s'affiche alors sur la carte avec les heures de passage aux diffférents points.

## Spécifications
Le fichier à charger contenant le programme de Pickup&delivery est au format xml et doit suivre le schéma suivant :
```
<planningRequest>
<depot address="342873658" departureTime="8:0:0"/>
<request pickupAddress="208769039" deliveryAddress="25173820" pickupDuration="180" deliveryDuration="240"/>
...
<request .../>
</planningRequest>
```
La ligne `depot` spécifie le départ et l'arrivée du livreur. L'`adrdress` est une id d'intersection et `departureTime` est son heure de départ.

Les lignes `request` décrivent les différentes livraisons à effecturer, qui comportent un point de retrait (pickup) et un point d'arrivée (delivery).
Les `pickupAddress` et `deliveryAddress` sont des id d'intersections, les `pickupDuration` et `deliveryDuration` sont les temps dont aura besoin le livreur sur place pour récupérer ou déposer la commande, en secondes.


## Diagramme de classes
TODO: à insérer

## Environnement technique
Le langage utilisé pour ce projet est Java 17, avec quelques parties en Kotlin également. 
Pour l'interface utilisateur, nous avons utilisé JavaFx et pour les tests, JUnit.

Cette application utlise également des librairies externes : [Jsprit](https://github.com/graphhopper/jsprit) et [graphhopper](https://github.com/graphhopper/graphhopper/), développées par Grahhopper. 
Ces librairies nous ont permis de résoudre l'algorithme du voyageur de commerce, avec les données d'OpenStreetMap.

