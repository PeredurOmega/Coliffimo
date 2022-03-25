Coliffimo
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

Une fois l'application lancée, une carte s'affiche. Cependant, pour pouvoir calculer des itinéraires, il faudra charger une carte à l'aide du bouton "Charger une carte'.

Ensuite, vous aurez la possibilité de charger un programme de Pickup&delivery, contenant les informations de la course à effectuer.

Le meilleur itinéraire s'affiche alors sur la carte avec les heures de passage aux diffférents points.

## Spécifications

### Cas d’utilisation 1 : Chargement d’un plan
*En tant qu’utilisateur, je veux charger un plan à partir d’un fichier XML afin de visualiser la ville.*

**Spécifications fonctionnelles :**
* Une carte de la ville s'affiche par défaut, carte de Rhône-Alpes chargée depuis OpenStreetMap
* L’utilisateur peut charger une carte au format .xml depuis l’interface.

Ce fichier n'est pas utilisé pour l'affichage, mais seulement pour relier les id des intersections avec leur lattitude et longitude.
Son format doit être le suivant :

```
<map>
<intersection id="208769499" latitude="45.760597" longitude="4.87622"/>
...
<intersection id="975886496" latitude="45.756874" longitude="4.8574047"/>
<segment destination="25175778" length="69.979805" name="Rue Danton" origin="25175791"/>
...
<segment destination="26033277" length="78.72686" name="Rue Danton" origin="975886496"/>
</map>
```
Les segments sont facultatifs, comme l'application ne les utlise pas pour calculer les itinéraires.
Leur `destination` et `origin` correspondent à des `id` d'intersection, et leur `length` est en mètres.

* L’utilisateur peut zoomer/dézoomer sur la carte mais aucune autre interaction n’est possible


### Cas d’utilisation 2 : Charger un programme de Pickup&delivery

*En tant qu’utilisateur, je veux charger un programme de Pickup&delivery à partir d’un fichier XML.*

**Spécifications fonctionnelles :**

* L’utilisateur peut charger un fichier .xml depuis l’interface

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

* L’utilisateur peut alors visualiser, sur la carte affichée précédemment :
    * Les points de départ représentés par des carrés
    * Les points d’arrivée représentés par des ronds
    * Les couples départ/arrivée correspondants sont repérés par des couleurs identiques

* L’utilisateur peut zoomer/dézoomer sur la carte mais aucune interaction avec les différents points n’est possible

### Cas d’utilisation 3 : Calculer la tournée pour un programme de Pickup&delivery
*En tant qu’utilisateur, je veux calculer la tournée d’un livreur pour un programme de Pickup&delivery afin de prévoir le trajet d’un livreur.*

**Spécifications fonctionnelles :**
* Le trajet du livreur est calculé lorsque le fichier XML est chargé par l'utilisateur.
* L’utilisateur peut alors visualiser, sur la carte affichée précédemment, le trajet proposé pour le livreur.
* L’utilisateur peut zoomer/dézoomer sur la carte.
* Une liste d'instructions décrivant l'itinéraire est affichée sur la droite. Elles peuvent être exportées au format JSON.

### Cas d’utilisation 4 : Modifier interactivement le programme
*En tant qu’utilisateur, je veux pouvoir modifier interactivement le programme afin de disposer d’une interface facilement utilisable.*

**Spécifications fonctionnelles :**
* L’utilisateur peut supprimer un couple de points ou en ajouter en cliquant sur la carte.
* L’utilisateur peut spécifier une fenêtre horaire pour le passage à un point donné.
* L'utilisateur peut cliquer sur un point de passage pour en voir les coordonnées
* L’utilisateur peut déplacer un point de passage en drag&drop

## Diagramme de classes
TODO: à insérer

## Environnement technique
Le langage utilisé pour ce projet est Java 17 également.
Pour l'interface utilisateur, nous avons utilisé JavaFx et pour les tests, JUnit.

Pour l'intégration continue, nous avons utilisé Gradle.

Cette application utlise également des librairies externes : [Jsprit](https://github.com/graphhopper/jsprit) et [graphhopper](https://github.com/graphhopper/graphhopper/), développées par Grahhopper.
Ces librairies nous ont permis de résoudre l'algorithme du voyageur de commerce, avec les données d'OpenStreetMap.
