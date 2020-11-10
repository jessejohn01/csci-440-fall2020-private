SELECT customers.Email from customers
inner join employees e on customers.SupportRepId = e.EmployeeId
inner join invoices i on customers.CustomerId = i.CustomerId
inner join invoice_items ii on i.InvoiceId = ii.InvoiceId
WHERE e.FirstName = 'Jane' AND e.LastName = 'Peacock'
group by customers.CustomerId having customers.CustomerId in (
    SELECT CustomerId from invoice_items
        inner join invoices i on invoice_items.InvoiceId = i.InvoiceId
        inner join tracks t on invoice_items.TrackId = t.TrackId
        inner join genres g on t.GenreId = g.GenreId
    WHERE g.Name = 'Rock');



SELECT CustomerId, g.Name from invoice_items
    inner join invoices i on invoice_items.InvoiceId = i.InvoiceId
    inner join tracks t on invoice_items.TrackId = t.TrackId
    inner join genres g on t.GenreId = g.GenreId
    WHERE g.Name = 'Rock';

