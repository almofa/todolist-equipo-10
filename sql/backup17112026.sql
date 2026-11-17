--
-- PostgreSQL database dump
--

\restrict H2LOU8DfHBUI3ZzrRvThRYE4HnkFvQeBRvykabheeUWSbwcIttqgIBUj9tMoqb0

-- Dumped from database version 13.23 (Debian 13.23-1.pgdg13+1)
-- Dumped by pg_dump version 13.23 (Debian 13.23-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.equipo_usuario DROP CONSTRAINT IF EXISTS fksabsvjgvfuen6hmyg7gn7oq4v;
ALTER TABLE IF EXISTS ONLY public.equipo_usuario DROP CONSTRAINT IF EXISTS fkk4y9gec15ccnirp3r7w29o66p;
ALTER TABLE IF EXISTS ONLY public.tareas DROP CONSTRAINT IF EXISTS fkdmoaxl7yv4q6vkc9h32wvbddr;
ALTER TABLE IF EXISTS ONLY public.usuarios DROP CONSTRAINT IF EXISTS usuarios_pkey;
ALTER TABLE IF EXISTS ONLY public.tareas DROP CONSTRAINT IF EXISTS tareas_pkey;
ALTER TABLE IF EXISTS ONLY public.equipos DROP CONSTRAINT IF EXISTS equipos_pkey;
ALTER TABLE IF EXISTS ONLY public.equipo_usuario DROP CONSTRAINT IF EXISTS equipo_usuario_pkey;
ALTER TABLE IF EXISTS public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.tareas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.equipos ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.usuarios_id_seq;
DROP TABLE IF EXISTS public.usuarios;
DROP SEQUENCE IF EXISTS public.tareas_id_seq;
DROP TABLE IF EXISTS public.tareas;
DROP SEQUENCE IF EXISTS public.equipos_id_seq;
DROP TABLE IF EXISTS public.equipos;
DROP TABLE IF EXISTS public.equipo_usuario;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: equipo_usuario; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipo_usuario (
    fk_equipo bigint NOT NULL,
    fk_usuario bigint NOT NULL
);


ALTER TABLE public.equipo_usuario OWNER TO mads;

--
-- Name: equipos; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipos (
    id bigint NOT NULL,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.equipos OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.equipos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.equipos_id_seq OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.equipos_id_seq OWNED BY public.equipos.id;


--
-- Name: tareas; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.tareas (
    id bigint NOT NULL,
    completada boolean NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);


ALTER TABLE public.tareas OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.tareas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tareas_id_seq OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.tareas_id_seq OWNED BY public.tareas.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean,
    bloqueado boolean,
    descripcion character varying(255),
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);


ALTER TABLE public.usuarios OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usuarios_id_seq OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: equipos id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id SET DEFAULT nextval('public.equipos_id_seq'::regclass);


--
-- Name: tareas id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas ALTER COLUMN id SET DEFAULT nextval('public.tareas_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: equipo_usuario; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipo_usuario (fk_equipo, fk_usuario) FROM stdin;
2	1
\.


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos (id, descripcion, nombre) FROM stdin;
1		Gandores
2	Somos los mejores	Tigres
\.


--
-- Data for Name: tareas; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.tareas (id, completada, titulo, usuario_id) FROM stdin;
2	f	ir al gym	1
3	f	quedar con pepe	1
4	t	revisar examen mads	1
1	t	hacer la compra	1
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.usuarios (id, admin, bloqueado, descripcion, email, fecha_nacimiento, nombre, password) FROM stdin;
1	f	f	\N	demo@ua.es	2015-07-17	demo	demo
\.


--
-- Name: equipos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.equipos_id_seq', 2, true);


--
-- Name: tareas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.tareas_id_seq', 4, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 1, true);


--
-- Name: equipo_usuario equipo_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT equipo_usuario_pkey PRIMARY KEY (fk_equipo, fk_usuario);


--
-- Name: equipos equipos_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_pkey PRIMARY KEY (id);


--
-- Name: tareas tareas_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT tareas_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: tareas fkdmoaxl7yv4q6vkc9h32wvbddr; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: equipo_usuario fkk4y9gec15ccnirp3r7w29o66p; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fkk4y9gec15ccnirp3r7w29o66p FOREIGN KEY (fk_equipo) REFERENCES public.equipos(id);


--
-- Name: equipo_usuario fksabsvjgvfuen6hmyg7gn7oq4v; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipo_usuario
    ADD CONSTRAINT fksabsvjgvfuen6hmyg7gn7oq4v FOREIGN KEY (fk_usuario) REFERENCES public.usuarios(id);


--
-- PostgreSQL database dump complete
--

\unrestrict H2LOU8DfHBUI3ZzrRvThRYE4HnkFvQeBRvykabheeUWSbwcIttqgIBUj9tMoqb0

