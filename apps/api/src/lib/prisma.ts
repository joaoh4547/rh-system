import {PrismaClient} from "../../generated/prisma";

export { Prisma } from "../../generated/prisma";

export const prisma = new PrismaClient({
	log: ["query", "error"],
});
