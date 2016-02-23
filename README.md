# SingBizExam-2


Exam

 

Discounted Items

 

1. Create a table like view that has column names reference #, product name, quantity and price.

2. Have a search box and go button. Reference number can be searched and when an item is found it will add on the table.

3. Use the data of products in data.csv (attached in this email)

	a. GST is already included in all retail price of all products.

4. Create a Category Table

	a. Add 5 record of Category and call it Cat A, B, C, D and E.

5. Random select some products and manually add it under each category.

6. Place four buttons: plus button, minus button, delete button and discount button.

	a. Plus button increases qty of a selected product in the table

	b. Minus button decreases qty of a selected product in table. Can decreases until to 1.

	c. Delete button removes the selected product in the table.

	d. Discount button opens a window where there is an input box, apply button, reset button.

7. Add a label where it will show that total quantity, total GST and total amount including GST.

8. GST is 7%.

 

Logic

 

1. Discount can be applied automatically or manually.

	a. If discount applied manually are added thru the discount button (6.d.).

2. All discounts are applied before GST.

3. Automatic discounts uses the following rules.

	a. Rule 1: every Wednesday, all items are less 10% between 2pm - 6pm.

	b. Rule 2: if the total amount of the selected item under Category B is more than or equal to SGD 1,000.00 then apply 40% discount.

		i. If mixed items are in the table, then just compute the total amount for the Category B.

	c. Rule 3: if its December 20th - 28th , all items under Category E are less 15 SGD.

	d. Rule 4: if the user wants to key-in a manual discount on an item that has currently automatic discount, the system should choose the higher percentage discount.

	e. Rule 5: if the total amount of items chosen for Category A and Category B reaches more than 500 SGD, (meaning Cat A 500 + and Cat B 500 +), then the system will give an additional 50 SGD discount. Meaning to say, items in the transaction need to be more than 500 for each of the categories before this additional $50 is given.
