--
-- PostgreSQL database dump
--

--
-- TOC entry 212 (class 1259 OID 16466)
-- Name: card_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE card_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 211 (class 1259 OID 16453)
-- Name: cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE cards (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    code character varying(36) NOT NULL,
    balance numeric(15,2) NOT NULL,
    status character varying NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    created_at timestamp(6) with time zone DEFAULT LOCALTIMESTAMP NOT NULL,
    cvc character varying(4),
    expiration_date bytea
);


--
-- TOC entry 214 (class 1259 OID 16480)
-- Name: transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- TOC entry 213 (class 1259 OID 16469)
-- Name: transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE transactions (
    id bigint NOT NULL,
    code character varying(36) NOT NULL,
    sender_card_id bigint NOT NULL,
    type character varying NOT NULL,
    amount numeric(15,2) NOT NULL,
    created_at timestamp without time zone DEFAULT LOCALTIMESTAMP NOT NULL,
    recipient_card_id bigint NOT NULL
);


--
-- TOC entry 210 (class 1259 OID 16450)
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- TOC entry 209 (class 1259 OID 16443)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE users (
    id bigint NOT NULL,
    username character varying(36) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    cardholder_name character varying NOT NULL
);

--
-- TOC entry 3358 (class 2606 OID 16534)
-- Name: cards Card_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY cards
    ADD CONSTRAINT "Card_pkey" PRIMARY KEY (id);


--
-- TOC entry 3360 (class 2606 OID 16502)
-- Name: transactions Transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY transactions
    ADD CONSTRAINT "Transaction_pkey" PRIMARY KEY (id);


--
-- TOC entry 3354 (class 2606 OID 16521)
-- Name: users User_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


--
-- TOC entry 3356 (class 2606 OID 16569)
-- Name: users users_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_unique UNIQUE (cardholder_name);


--
-- TOC entry 3365 (class 2620 OID 16468)
-- Name: cards card_id_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER card_id_trigger BEFORE INSERT ON cards FOR EACH ROW EXECUTE FUNCTION card_id_trigger_func();


--
-- TOC entry 3366 (class 2620 OID 16482)
-- Name: transactions transaction_id_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER transaction_id_trigger BEFORE INSERT ON transactions FOR EACH ROW EXECUTE FUNCTION transaction_id_trigger_func();


--
-- TOC entry 3364 (class 2620 OID 16452)
-- Name: users user_id_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER user_id_trigger BEFORE INSERT ON users FOR EACH ROW EXECUTE FUNCTION user_id_trigger_func();


--
-- TOC entry 3361 (class 2606 OID 16546)
-- Name: cards Card_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY cards
    ADD CONSTRAINT "Card_userid_fkey" FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 3363 (class 2606 OID 16584)
-- Name: transactions fk_recipient_card_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY transactions
    ADD CONSTRAINT fk_recipient_card_id FOREIGN KEY (recipient_card_id) REFERENCES cards(id);


--
-- TOC entry 3362 (class 2606 OID 16579)
-- Name: transactions fk_sender_card_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY transactions
    ADD CONSTRAINT fk_sender_card_id FOREIGN KEY (sender_card_id) REFERENCES cards(id);


-- Completed on 2025-08-30 11:48:17 CEST

--
-- PostgreSQL database dump complete
--

