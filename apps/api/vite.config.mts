import { defineConfig } from "vitest/config";
import tsConfigPaths from "vite-tsconfig-paths";

export default defineConfig({
	plugins: [tsConfigPaths()],
	test: {
		globals: true,
		coverage: {
			all: true,
			provider: "v8",
		},
	},
});
