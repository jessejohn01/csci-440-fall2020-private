

SELECT employees.FirstName as FirstName, employees.LastName as LastName, employees.Email as Email, COUNT(invoices.InvoiceId) as SalesCount, ROUND(Sum(invoices.Total),2) as SalesTotal FROM employees
JOIN customers on employees.EmployeeId = customers.SupportRepId
JOIN invoices on customers.CustomerId = invoices.CustomerId
GROUP BY employees.FirstName, employees.LastName, employees.Email;