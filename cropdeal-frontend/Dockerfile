# ==========================================
# Stage 1: Build Angular Application
# ==========================================
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies first for optimal layer caching
COPY package.json package-lock.json ./
RUN npm ci

# Copy application source code
COPY . .

# Build production bundle
RUN npm run build -- --configuration production

# ==========================================
# Stage 2: Serve with Production Nginx Alpine
# ==========================================
FROM nginx:alpine

# Remove default nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration with Angular SPA routing & compression
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from build stage
COPY --from=build /app/dist/cropdeal-frontend/browser /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
