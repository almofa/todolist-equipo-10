-- Migración del esquema de la versión 1.2.0 a la 1.3.0
ALTER TABLE public.usuarios ADD COLUMN descripcion character varying(255);
ALTER TABLE public.equipos ADD COLUMN descripcion character varying(255);
ALTER TABLE public.tareas ADD COLUMN completada boolean NOT NULL DEFAULT false;