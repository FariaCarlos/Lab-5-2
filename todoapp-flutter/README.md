# todoapp

A Flutter project to show tasks (CNCS / ESS / Lab3).

## Configuração

### Variáveis de ambiente (.env)

- `HOST`: Endereço do servidor backend
  - Emulador Android: `10.0.2.2`
  - iOS Simulator/Desktop: `localhost`
- `PORT`: Porto do servidor (default: 7100)

### Notas de Segurança

O código atual usa `SharedPreferences` para guardar o token de autenticação.
Para aplicações de produção, deve ser usado `flutter_secure_storage`
que utiliza Keychain (iOS) e Keystore (Android).
