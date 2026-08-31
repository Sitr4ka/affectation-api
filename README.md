# affectPro

## API REST pour le frontend React

L'application ecoute le port `80` par defaut. Si ce port est occupe,
lancez Spring avec `SERVER_PORT=8080` puis configurez la base URL du client en
consequence.

| Methode | URL | Resultat |
| --- | --- | --- |
| `GET`, `POST` | `/api/v1/employees` | Employes |
| `GET`, `PUT`, `DELETE` | `/api/v1/employees/{id}` | Employe cible |
| `GET`, `POST` | `/api/v1/locations` | Lieux |
| `GET`, `PUT`, `DELETE` | `/api/v1/locations/{id}` | Lieu cible |
| `GET`, `POST` | `/api/v1/assignments` | Affectations |
| `GET`, `PUT`, `DELETE` | `/api/v1/assignments/{id}` | Affectation cible |
| `GET` | `/api/v1/employees/{id}/assignments` | Historique d'un employe |
| `GET` | `/api/v1/dashboard/summary` | Chiffres du tableau de bord |

Les routes historiques suivantes sont egalement prises en charge. Elles
conservent les enveloppes JSON attendues par l'interface (`success`, `employe`,
`lieu`, `affectation`) et les noms de champs historiques (`numEmp`, `idLieu`,
`numAffect`). Elles permettent une migration progressive vers `/api/v1`.

| Route | Ressource |
| --- | --- |
| `/affect-pro/backend/employeApi.php` | Employes |
| `/affect-pro/backend/placeApi.php` | Lieux |
| `/affect-pro/backend/affectationApi.php` | Affectations |

Les trois routes acceptent `GET`, `POST`, `PUT` et `DELETE`; `GET` avec
`?action=nextId` fournit le prochain identifiant affiche dans les formulaires.

Exemple de corps pour `POST` ou `PUT` :

```json
{
  "employeeId": "uuid-employe",
  "newLocationId": "uuid-lieu",
  "effectiveDate": "2026-08-31",
  "reason": "Mutation"
}
```
