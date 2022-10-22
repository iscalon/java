import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.lang3.ArrayUtils;

import org.nico.ast.interfaces.Node;
import org.nico.ast.nodes.SyntacticAndNode;
import org.nico.ast.nodes.SyntacticFalseNode;
import org.nico.ast.nodes.SyntacticNotNode;
import org.nico.ast.nodes.SyntacticOrNode;
import org.nico.ast.nodes.SyntacticReferenceExpressionNode;
import org.nico.ast.nodes.SyntacticTrueNode;
import org.nico.ast.visitors.StringNodeVisitor;
import org.nico.engine.internal.jexl.ASTTransformerVisitor;
import org.nico.engine.internal.jexl.NicoEngine;
import org.nico.engine.internal.jexl.NicoJexlBuilder;

/**
 * Utilitaire de transformation d'AST
 */
public class ASTTransformUtil {

    /**
     * La méthode de combinaison d'un AST avec un autre.
     */
    public enum COMBINING_METHOD {
        OR(OR_COMBINE_OPERATOR, falseAST()),
        AND(AND_COMBINE_OPERATOR, trueAST());

        /** L'opération de combinaison de 2 éléments */
        private BinaryOperator<AST> combiningOperation;

        /** L'identité pour l'opération de combinaison (ex : pour le 'ou' logique c'est 'false', pour le 'et' logique c'est 'true') */
        private AST identity;

        COMBINING_METHOD(BinaryOperator<AST> combiningOperation, AST identity) {
            this.combiningOperation = combiningOperation;
            this.identity = identity;
        }

        public BinaryOperator<AST> combiningOperation() {
            return combiningOperation;
        }

        public AST identity() {
            return identity;
        }
    }

    private static final BinaryOperator<AST> OR_COMBINE_OPERATOR = (AST ast1, AST ast2) -> {
        AST ret = new AST();
        Node childAST1 = ast1.getChild(0);
        Node childAST2 = ast2.getChild(0);

        if (ast1.equals(ast2)) {
            // On a 2 fois le même arbre donc on ne le met qu'une fois.
            ret.addChild(childAST1);
            return ret;
        }
        if (isTrueAST(ast1) || isTrueAST(ast2)) {
            // Si on a un AST qui est 'true' alors on renvoit 'true'
            ret.addChild(new SyntacticTrueNode());
            return ret;
        }
        if (isFalseAST(ast1)) {
            // L'AST1 est 'false' donc on peut directement renvoyer l'AST2
            ret.addChild(childAST2);
            return ret;
        }
        if (isFalseAST(ast2)) {
            // L'AST2 est 'false' donc on peut directement renvoyer l'AST1
            ret.addChild(childAST1);
            return ret;
        }
        // On va combiner les 2 arbres avec un 'OU' logique.
        SyntacticReferenceExpressionNode refExpr1 = new SyntacticReferenceExpressionNode();
        refExpr1.addChild(childAST1);
        SyntacticReferenceExpressionNode refExpr2 = new SyntacticReferenceExpressionNode();
        refExpr2.addChild(childAST2);

        SyntacticOrNode or = new SyntacticOrNode();
        or.addChild(refExpr1);
        or.addChild(refExpr2);
        ret.addChild(or);

        return ret;
    };

    private static final BinaryOperator<AST> AND_COMBINE_OPERATOR = (AST ast1, AST ast2) -> {
        AST ret = new AST();
        Node childAST1 = ast1.getChild(0);
        Node childAST2 = ast2.getChild(0);

        if (ast1.equals(ast2)) {
            // On a 2 fois le même arbre donc on ne le met qu'une fois.
            ret.addChild(childAST1);
            return ret;
        }
        if (isFalseAST(ast1) || isFalseAST(ast2)) {
            // Si on a un AST qui est 'false' alors on renvoit 'false'
            ret.addChild(new SyntacticFalseNode());
            return ret;
        }
        if (isTrueAST(ast1)) {
            // L'AST1 est 'true' donc on peut directement renvoyer l'AST2
            ret.addChild(childAST2);
            return ret;
        }
        if (isTrueAST(ast2)) {
            // L'AST2 est 'true' donc on peut directement renvoyer l'AST1
            ret.addChild(childAST1);
            return ret;
        }

        // On va combiner les 2 arbres avec un 'ET' logique.
        SyntacticReferenceExpressionNode refExpr1 = new SyntacticReferenceExpressionNode();
        refExpr1.addChild(childAST1);
        SyntacticReferenceExpressionNode refExpr2 = new SyntacticReferenceExpressionNode();
        refExpr2.addChild(childAST2);

        SyntacticAndNode and = new SyntacticAndNode();
        and.addChild(refExpr1);
        and.addChild(refExpr2);
        ret.addChild(and);

        return ret;
    };

    private ASTTransformUtil() {
        // does nothing
    }

    /**
     * Transforme une expression en un arbre de la syntaxe abstraite {@link AST} lui correspondant.
     *
     * @param expression l'expression dont on souhaite obtenir l'arbre de la syntaxe abstraite.
     * @return un {@link AST} correspondant à l'arbre jexl transformé.
     */
    public static AST transformExpressionToAST(String expression) {
        return transformExpressionToAST(expression, Collections.emptyMap());
    }

    /**
     * Transforme une expression en un arbre de la syntaxe abstraite {@link AST} lui correspondant.
     *
     * @param expression l'expression dont on souhaite obtenir l'arbre de la syntaxe abstraite.
     * @param context le contexte jexl aidant pour la résolution des variables, c'est une map associant à chaque nom de variable sa valeur. Peut-être <code>null</code>.
     * @return un {@link AST} correspondant à l'arbre jexl transformé.
     */
    public static AST transformExpressionToAST(String expression, Map<String, Object> context) {
        // Création du JexlContext à partir de celui passé en paramètre.
        JexlContext jexlContext = new MapContext(context);

        // Création du moteur Jexl pour obtenir un AST Jexl
        NicoEngine engine = (NicoEngine) new NicoJexlBuilder().create();
        engine.createScript(expression);
        ASTJexlScript jexlAST = engine.getAST();

        // Appel de notre visiteur pour transformer l'AST Jexl en notre AST.
        return transformJexlAST(jexlAST, jexlContext);
    }

    /**
     * Retourne l'AST "négation" de l'expression représentée par l'AST passé en paramètre.<br>
     * Si le noeud enfant de l'AST est un noeud 'booléen' alors on utilise directement sa négation.
     *
     * @param ast
     * @return
     */
    public static AST negateAST(AST ast) {
        if (Objects.isNull(ast)) {
            return null;
        }
        if (ast.getChildrenCount() != 1) { // Un noeud AST ne doit avoir qu'un seul enfant.
            throw new IllegalStateException("Un noeud 'AST' ne peut et doit avoir qu'un seul enfant");
        }
        AST ret = new AST();
        Node child = ast.getChild(0);
        Node notNode = new SyntacticNotNode();
        if (child instanceof SyntacticTrueNode) {
            notNode = new SyntacticFalseNode();
        } else if (child instanceof SyntacticFalseNode) {
            notNode = new SyntacticTrueNode();
        } else {
            notNode.addChild(child);
        }
        ret.addChild(notNode);
        return ret;
    }

    /**
     * Transforme un {@link AST} sous forme de String.
     *
     * @param ast l'arbre à transformer en String
     * @return une chaine de caractères représentant l'arbre passé en paramètre.
     */
    public static String transformASTToString(AST ast) {
        StringNodeVisitor visitor = new StringNodeVisitor();
        return (String) visitor.visit(ast);
    }

    /**
     * Transforme un arbre de la syntaxe abstraite provenant de jexl en un {@link AST}.
     *
     * @param jexlAST l'arbre à transformer.
     * @param context le contexte jexl aidant pour la résolution des variables.
     * @return un {@link AST} correspondant à l'arbre jexl transformé.
     */
    static AST transformJexlAST(ASTJexlScript jexlAST, JexlContext context) {
        ASTTransformerVisitor visitor = new ASTTransformerVisitor(context);
        return (AST) visitor.visit(jexlAST, null);
    }

    /**
     * Combine des AST entre eux suivant des fonctions logiques 'OR' ou 'AND'.<br><br>
     * Les AST qui ne possèdent qu'un noeud {@link SyntacticFalseNode} et dont la méthode de combinaison est 'OR' sont retirés des combinaisons.<br><br>
     * Les AST qui ne possèdent qu'un noeud {@link SyntacticTrueNode} et dont la méthode de combinaison est 'AND' sont retirés des combinaisons.<br><br>
     * Sont d'abord combinés les AST avec des méthodes 'OR' puis ceux avec des méthodes 'AND'.<br><br>
     * <u>NB</u> : L'ordre de combinaison ne respecte pas forcément l'ordre d'apparition des arbres.
     *
     * @param asts l'ensemble des AST à combiner avec leurs méthodes associées.
     * @return un {@link AST} résultant de la combinaison de tous les autres.
     *
     * @see COMBINING_METHOD
     */
    public static Optional<AST> combineASTs(Map<AST, COMBINING_METHOD> asts) {
        return combineASTs(convertMapToEntryArray(asts));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Entry<K, V>[] convertMapToEntryArray(Map<K, V> attributes) {
        return attributes.entrySet().stream()
                        .toArray(size -> (Entry<K, V>[]) Array.newInstance(Entry.class, size));
    }

    /**
     * Combine des AST entre eux suivant des fonctions logiques 'OR' ou 'AND'.<br><br>
     * Les AST qui ne possèdent qu'un noeud {@link SyntacticFalseNode} et dont la méthode de combinaison est 'OR' sont retirés des combinaisons.<br><br>
     * Les AST qui ne possèdent qu'un noeud {@link SyntacticTrueNode} et dont la méthode de combinaison est 'AND' sont retirés des combinaisons.<br><br>
     * Sont d'abord combinés les AST avec des méthodes 'OR' puis ceux avec des méthodes 'AND'.<br><br>
     * <u>NB</u> : L'ordre de combinaison ne respecte pas forcément l'ordre d'apparition des arbres.
     *
     * @param asts l'ensemble des AST à combiner avec leurs méthodes associées.
     * @return un {@link AST} résultant de la combinaison de tous les autres.
     *
     * @see COMBINING_METHOD
     */
    @SafeVarargs
    public static Optional<AST> combineASTs(Map.Entry<AST, COMBINING_METHOD>... asts) {
        if (ArrayUtils.isEmpty(asts)) {
            return Optional.empty();
        }
        if (ArrayUtils.getLength(asts) == 1) {
            return Optional.ofNullable(asts[0].getKey());
        }

        Map<COMBINING_METHOD, Set<AST>> group = Arrays.stream(asts)
                        .collect(Collectors.groupingBy(Map.Entry::getValue, // On groupe par méthode
                                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet()))); // On associe les AST correspondants à chaque méthode.

        // Combinaison des 'OR'
        Optional<AST> orCombinedAST = orCombining(group.get(COMBINING_METHOD.OR));

        // Combinaison des 'AND'
        Optional<AST> andCombinedAST = andCombining(group.get(COMBINING_METHOD.AND));

        if (!orCombinedAST.isPresent()) {
            return andCombinedAST;
        }
        if (!andCombinedAST.isPresent()) {
            return orCombinedAST;
        }

        // On combine les 2 sous-arbres résultat
        Set<AST> both = new HashSet<>();
        both.add(orCombinedAST.get());
        both.add(andCombinedAST.get());
        return andCombining(both);
    }

    /**
     * S'occupe de combiner avec des 'OR' les AST passés en paramètre.
     *
     * @param group
     * @return l'AST combinaison avec des 'OR' de ceux passés en paramètre.
     */
    public static Optional<AST> orCombining(Collection<AST> group) {
        return genericCombining(group, COMBINING_METHOD.OR);
    }

    /**
     * S'occupe de combiner avec des 'AND' les AST passés en paramètre.
     *
     * @param group
     * @return l'AST combinaison avec des 'AND' de ceux passés en paramètre.
     */
    public static Optional<AST> andCombining(Collection<AST> group) {
        return genericCombining(group, COMBINING_METHOD.AND);
    }

    static Optional<AST> genericCombining(Collection<AST> group, COMBINING_METHOD method) {
        if (CollectionUtils.isEmpty(group)) {
            return Optional.empty();
        }

        // On retire les ASTs vides ou null des autres et en même temps on enlève (grâce au 'Set') les arbres doublons.
        Set<AST> filteredGroup = group.stream().filter(ast -> Objects.nonNull(ast) && ast.getChildrenCount() > 0).collect(Collectors.toSet());
        if (filteredGroup.isEmpty()) {
            return Optional.of(new AST()); // AST vide
        }

        // On va combiner les arbres restants entre eux.
        return filteredGroup.stream().reduce(method.combiningOperation());
    }

    public static AST falseAST() {
        AST falseAST = new AST();
        falseAST.addChild(new SyntacticFalseNode());
        return falseAST;
    }

    public static AST trueAST() {
        AST trueAST = new AST();
        trueAST.addChild(new SyntacticTrueNode());
        return trueAST;
    }

    public static boolean isFalseAST(AST ast) {
        if (Objects.isNull(ast)) {
            return false;
        }
        return ast.getChildrenCount() == 1 && ast.getChild(0) instanceof SyntacticFalseNode;
    }

    public static boolean isTrueAST(AST ast) {
        if (Objects.isNull(ast)) {
            return false;
        }
        return ast.getChildrenCount() == 1 && ast.getChild(0) instanceof SyntacticTrueNode;
    }

    public static boolean isBooleanAST(AST ast) {
        return isFalseAST(ast) || isTrueAST(ast);
    }

    public static boolean areBooleanASTEquals(AST ast1, AST ast2) {
        if (isBooleanAST(ast1) && isBooleanAST(ast2)) {
            return isTrueAST(ast1) && isTrueAST(ast2) || isFalseAST(ast1) && isFalseAST(ast2);
        }
        return false;
    }
}