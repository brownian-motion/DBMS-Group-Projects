/* DDL */
CREATE TABLE Student(
  login varchar(50),
  sname varchar(50),
  university varchar(50),
  grad_year int,
  PRIMARY KEY (login)
);

CREATE TABLE Contest(
  cname varchar(50),
  year int,
  location varchar(50)
  PRIMARY KEY (cname)
)

CREATE TABLE Participated(
  login varchar(50),
  cname varchar(50),
  PRIMARY KEY (login, cname),
  FOREIGN KEY (login) REFERENCES Student(login),
  FOREIGN KEY (cname) REFERENCES Contest(cname)
);

CREATE TABLE Author(
  aid int,
  aname varchar(50),
  compensation int,
  PRIMARY KEY (aid)
);

CREATE TABLE Problem(
  pid int,
  pname varchar(50),
  max_score int,
  aid int,
  FOREIGN KEY (aid) REFERENCES Author(aid),
  PRIMARY KEY (pid)
);

CREATE TABLE Scored(
  pid int,
  login varchar(50),
  score REAL,
  FOREIGN KEY (pid) REFERENCES Problem(pid),
  FOREIGN KEY (login) REFERENCES Student(login),
  PRIMARY KEY (pid, login)
);

CREATE TABLE Contest_Problems(
  cname varchar(50),
  pid int,
  FOREIGN KEY (cname) REFERENCES Contest(cname),
  FOREIGN KEY (pid) REFERENCES Problem(pid),
  PRIMARY KEY (cname, pid)
);


/* Insertions */
INSERT INTO Author VALUES (101, 'Alexander Sforza',  2000),
                          (102, 'Rachel Moran',      1500),
                          (103, 'David Terans',      2000),
                          (104, 'Elizabeth Forster', 2500);

INSERT INTO Contest VALUES ('Week of Code',          2016, 'Norman, OK'),
                           ('University Codesprint', 2017, 'College Station, TX'),
                           ('Hour Rank',             2018, 'Norman, OK');

INSERT INTO Contest_Problems VALUES ('Week of Code',          10),
                                    ('Week of Code',          11),
                                    ('Week of Code',          12),
                                    ('Week of Code',          13),
                                    ('University Codesprint', 14),
                                    ('University Codesprint', 15),
                                    ('University Codesprint', 16),
                                    ('Hour Rank',             17),
                                    ('Hour Rank',             18),
                                    ('Hour Rank',             19);

INSERT INTO Participated VALUES ('1udaya1',     'Week of Code'),
                                ('mightybruce', 'Week of Code'),
                                ('jose1980',    'Week of Code'),
                                ('moerman',     'University Codesprint'),
                                ('grice',       'University Codesprint'),
                                ('stefan',      'University Codesprint'),
                                ('stefan',      'Hour Rank'),
                                ('cfox',        'Hour Rank'),
                                ('_ash_',       'Hour Rank');

INSERT INTO Problem VALUES (10, 'Exceeding the Speed Limit', 10, 101),
                           (11, 'Array Triplets', 20, 102),
                           (12, 'a,b Special Points', 30, 104),
                           (13, 'Cube-loving Numbers', 50, 103),
                           (14, 'Interesting Trip', 15, 104),
                           (15, 'Sword Profit', 20, 103),
                           (16, 'Which Section?', 40, 103),
                           (17, 'Minute to Win It', 10, 103),
                           (18, 'Watson''s Love for Arrays', 30, 102),
                           (19, 'Dynamic Trees', 50, 102);

INSERT INTO Scored VALUES (10, '1udaya1',      10),
                          (10, 'mightybruce',  8),
                          (10, 'jose1980',     51),
                          (11, '1udaya1',      20),
                          (11, 'jose1980',     8),
                          (12, 'mightybruce',  10),
                          (12, 'jose1980',     20),
                          (13, 'jose1980',     35),
                          (14, 'moerman',      10),
                          (14, 'grice',        15),
                          (14, 'stefan',       14),
                          (15, 'moerman',      19),
                          (15, 'stefan',       18),
                          (16, 'grice',        33),
                          (17, 'stefan',       10),
                          (17, '_ash_',        10),
                          (17, 'cfox',         9),
                          (18, 'stefan',       27),
                          (18, '_ash_',        18),
                          (19, 'stefan',       41);

INSERT INTO Student VALUES ('stefan',      'Stefan Keller',     'University of Oklahoma',     2020),
                           ('cfox',        'Colin Fox',         'Oklahoma State University',  2021),
                           ('moerman',     'Fientje Moerman',   'Oklahoma State University',  2019),
                           ('grice',       'George Rice',       'Texas A&M University',       2019),
                           ('1udaya1',     'Udaya Chandrika',   'Baylor University',          2018),
                           ('mightybruce', 'Bruce Yamashita',   'Texas A&M University',       2018),
                           ('_ash_',       'Ashley Brzozowicz', 'University of Oklahoma',     2020),
                           ('jose1980',    'Jose Monteiro',     'Texas Christian University', 2018);


/** Problem 1 **/

CREATE INDEX Problem_aid_index
ON Problem(aid);

/* Problem 2.5 */
SELECT pname
FROM Problem
WHERE max_score=(SELECT MAX(max_score) FROM Problem) 
AND 
Problem.aid=(SELECT Author.aid FROM Author WHERE Author.aname='Rachel Moran');

/* Problem 2.7 */
SELECT cname, SUM(compensation)
FROM Contest_Problems 
LEFT JOIN Problem ON Contest_Problems.pid = Problem.pid
LEFT JOIN Author ON Problem.aid = Author.aid
GROUP BY cname;


/** Problem 2: Stored Procedures **/

/* Option 1 */
CREATE PROCEDURE option_1
    @pid INT,
    @pname varchar(50),
    @aid INT
AS
  BEGIN
    DECLARE @maxscore INT;
    IF EXISTS( SELECT aid from Problem where aid = @aid)
      SET @maxscore = (SELECT ROUND(avg(max_score) * 1.1, 0) FROM Problem WHERE aid = @aid);
    ELSE
      SET @maxscore = (SELECT ROUND(avg(max_score), 0) FROM Problem);


    INSERT INTO Problem(pid, pname, max_score, aid) VALUES (@pid, @pname, @maxscore, @aid);
  END

/* Option 2 */
CREATE PROCEDURE Give_Author_Raise
    @aid Int
AS
BEGIN
    UPDATE Author SET compensation = compensation * (1 + raise)
    FROM Author
       LEFT JOIN
         (
           SELECT Author.aid, num_problems,
                  CASE
                    WHEN problem_count_rank = 1 THEN 0.20
                    WHEN problem_count_rank = 2 THEN 0.15
                    WHEN problem_count_rank = 3 THEN 0.10
                    ELSE 0.05
                  END AS raise
           FROM
                (
                SELECT aid, COUNT(pid) AS num_problems, RANK() OVER (ORDER BY COUNT(pid) DESC) AS problem_count_rank
                FROM Problem
                GROUP BY aid
                ) AS problem_count_ranks
                LEFT JOIN Author
                ON Author.aid = problem_count_ranks.aid
           ) AS raises
         ON (Author.aid = raises.aid)
        WHERE Author.aid = @aid
END

-- EXEC Give_Author_Raise @aid = 103;