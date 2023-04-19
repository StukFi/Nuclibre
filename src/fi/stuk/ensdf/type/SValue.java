/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf.type;

/**
This field may contain no more than three S-values, in the form of NUM defined in
V.9, separated by a '+' or a comma, for corresponding L-values given in the L-field
(col. 65-74). Parentheses are allowed and will be interpreted to mean probable values.
<p>
V.9 These fields consist of either a blank or a single unsigned number (NUM) in one of
the following forms:
<ol>
<li>1. An integer (e.g., 345)</li>
<li>2. A real number (e.g., 345.23)</li>
<li>3. An integer followed by an integer exponent (e.g., 345E-4, 4E+5)</li>
<li>4. A real number followed by an integer exponent (e.g., 345.E-4)</li>
</ol>
Note: It is desirable to write a number as '0.345' rather than
'.345'.
 * @author Tero Karhunen
 */
public class SValue {

    public SValue(){
    }

    public SValue(String str){
        this();
    }

}
