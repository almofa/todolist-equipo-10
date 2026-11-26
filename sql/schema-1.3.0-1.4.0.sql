-- Migración del esquema de la versión 1.3.0 a la 1.4.0
ALTER TABLE public.tareas ADD COLUMN prioridad character varying(255) NOT NULL DEFAULT 'MEDIA';