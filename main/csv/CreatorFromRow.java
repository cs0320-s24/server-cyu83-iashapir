package csv;

import edu.brown.cs.student.main.FactoryFailureException;

import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 *
 * <p>what should we do with the header though? should it also be an object of this type?
 *
 * <p>Your parser class constructor should take a second parameter of this generic interface type.
 *
 * <p>CHANGES:
 */
public interface CreatorFromRow<T> {
  T create(List<String> row) throws FactoryFailureException;
}
