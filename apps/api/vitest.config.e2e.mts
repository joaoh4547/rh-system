import swc from "unplugin-swc";
import {configDefaults, defineConfig} from "vitest/config";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
	test: {
		include: ["**/*.e2e-spec.ts"],
		globals: true,
		root: "./",
		exclude: [...configDefaults.exclude, "**/data/**"],
		setupFiles: ["./test/setup-e2e.ts"],
	},
	plugins: [
		tsconfigPaths(),
		swc.vite({
			module: { type: "es6" },
		}),
	],
});
