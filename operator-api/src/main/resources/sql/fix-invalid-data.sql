-- Fix invalid language values in operators table
UPDATE operators SET language = 'JAVA' WHERE language = '0' OR language IS NULL OR language = '';
