# ==========================================
# CropDeal Angular Frontend - Production Image
# ==========================================
FROM nginx:alpine

LABEL maintainer="CropDeal Engineering Team"
LABEL description="Production Nginx image for CropDeal Angular Frontend"

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration with Angular SPA routing & compression
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy pre-compiled production bundle into Nginx web root
COPY dist/cropdeal-frontend/browser /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

# Built-in health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
