CREATE TABLE ftblNews (
  newsID INT PRIMARY KEY,
  title VARCHAR(255),
  language VARCHAR(255),
  source_id VARCHAR(255),
  pubDate VARCHAR(255)
);

CREATE TABLE category (
  cID INT PRIMARY KEY,
  categoryName VARCHAR(255)
);

CREATE TABLE hasTable (
  newsID INT,
  cID INT,
  FOREIGN KEY (newsID) REFERENCES ftblNews(newsID),
  FOREIGN KEY (cID) REFERENCES category(cID)
);
