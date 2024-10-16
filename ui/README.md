# webcurator-ui

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

### Deploy in production environment
The webcurator-ui can be packaged to jar file and installed into local Maven Repository. The webcurator-ui has been integrated to webcurator-webapp and served by webcurator-webapp.

```sh
./gradlew clear install
```

Then you can build and run webcurator-webapp. The access url of the new ui: http://localhost:8080/wct/index.html.

A link is put in the existing ui for the benefit of new ui developers. You can login into http://localhost:8080/wct/curator/home.html, and click the "New UI" link on the top right to open the new ui.

