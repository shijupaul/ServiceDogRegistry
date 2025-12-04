-- Supplier

-- (1)
INSERT INTO supplier (code, name, contact_person, phone, email, version)
VALUES ( 'ELITE_K9', 'Elite K9 Training Center', 'John Smith', '555-0101', 'john@elitek9.com', 1);

-- (2)
INSERT INTO supplier (code, name, contact_person, phone, email, version)
VALUES ('ALPHA_DOG','Alpha Dog Breeders', 'Jane Doe', '555-0102', 'jane@alphadog.com', 1);

-- (3)
INSERT INTO supplier (code, name, contact_person, phone, email, version)
VALUES ('BRAVO_CANINES','Bravo Canines Inc.', 'Mike Johnson', '555-0103', 'mike@bravocanines.com', 1);

-- PoliceDog

-- deleted
-- (1)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, deleted_at, version, supplier_id)
VALUES ('Bella', 'Dutch Shepherd', 'FEMALE', '2018-11-25', '2019-06-10', 'IN_SERVICE', 'K9-004', false, false, true, false, NULL, true, 'Requires daily running and agility training.', true, 'Allergic to certain medications; requires special veterinary care.', 'CALM', true, '2023-01-01 10:15:30',1,3);

-- LEFT status
-- (2)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, leaving_date, leaving_reason, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Charlie', 'Belgian Malinois', 'MALE', '2020-02-14', '2020-09-20', 'LEFT', 'K9-005', '2023-09-25', 'RETIRED_PUT_DOWN', true, true, false, true, 'Grain-free diet due to allergies.', true, 'Needs regular check-ups for hip dysplasia.', false, NULL, NULL, false,1,2);

-- RETIRED status
-- (3)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Daisy', 'German Shepherd', 'FEMALE', '2019-07-30', '2020-03-15', 'RETIRED', 'K9-006', false, false, true, false, NULL, true, 'Enjoys swimming and fetch games.', false, NULL, 'FRIENDLY', false, 1,1);

-- TRAINING status
-- (4)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, deleted, version, supplier_id)
VALUES ('Luna', 'German Shepherd', 'FEMALE', '2021-03-10', '2021-10-15', 'TRAINING', 'K9-003', false, 1,2);
-- (5)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Rocky', 'Dutch Shepherd', 'MALE', '2021-01-05', '2021-08-12', 'TRAINING', 'K9-007', true, true, false, true, 'Low-fat diet for weight management.', true, 'Prone to ear infections; requires regular cleaning.', false, NULL,'AGGRESSIVE', false, 1,3);
-- (6)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Zoey', 'Dutch Shepherd', 'FEMALE', '2020-09-22', '2021-04-30', 'TRAINING', 'K9-010', true, true, false, true, 'High-protein diet for energy.', true, 'Requires monitoring for allergies.', false, NULL, 'AGGRESSIVE', false, 1,3);

-- IN_SERVICE status
-- (7)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, deleted, version, supplier_id)
VALUES ('Rex', 'German Shepherd', 'MALE', '2020-05-15', '2020-12-15', 'IN_SERVICE', 'K9-001', false, 1,1);
-- (8)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, deleted, version, supplier_id)
VALUES ('Max', 'Belgian Malinois', 'MALE', '2019-08-20', '2020-02-15', 'IN_SERVICE', 'K9-002', false, 1,1);
-- (9)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Molly', 'Belgian Malinois', 'FEMALE', '2018-04-18', '2018-11-22', 'IN_SERVICE', 'K9-008', false, false, true, false, NULL, true, 'Loves agility courses and scent tracking.', false, NULL, 'CALM', false, 1,2);
-- (10)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Buddy', 'German Shepherd', 'MALE', '2019-12-09', '2020-06-18', 'IN_SERVICE', 'K9-009', false, false, true, true, 'Sensitive stomach; requires special food.', true, 'Tends to develop joint issues; needs supplements.', false, NULL,'FRIENDLY', false, 1,1);
-- (11)
INSERT INTO police_dog (name, breed, gender, birth_date, date_acquired, status, badge_number, is_aggressive, requires_separate_kennel, is_noice_tolerant, has_special_diet, dietary_requirements, requires_exercise, exercise_notes, has_medical_conditions, medical_notes, temperament, deleted, version, supplier_id)
VALUES ('Sadie', 'Belgian Malinois', 'FEMALE', '2017-10-12', '2018-05-08', 'IN_SERVICE', 'K9-011', false, false, true, false, NULL, true, 'Enjoys long walks and playtime.', false, NULL, 'CALM', false, 1,2);