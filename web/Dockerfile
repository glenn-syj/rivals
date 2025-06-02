# syntax=docker/dockerfile:1.4
FROM node:20-alpine AS runtime

WORKDIR /app

COPY .next/standalone ./.next/standalone
COPY .next/static ./.next/standalone/web/.next/static
COPY public ./.next/standalone/web/public

EXPOSE 3000
CMD ["node", "./.next/standalone/web/server.js"]