# MiniLang — Intérprete con ANTLR4

**Trabajo Práctico Cuatrimestral**  
Conceptos y Paradigmas de Lenguajes de Programación — 2026  
Universidad Nacional de Lanús
Grupo AC

---

## Integrantes

| Nombre |
|--------|
| Matías Acosta Mlodziejewski |
| Francisco Patejim |

---

## Variante asignada

**Variante 4 — `repeat-until`**

Iteración con condición de corte al final e invertida: el cuerpo se ejecuta al menos una vez y el loop continúa mientras la condición sea falsa, deteniéndose cuando se vuelve verdadera.

---

## Descripción del lenguaje

MiniLang es un lenguaje de programación imperativo simple, de tipado estático y sintaxis inspirada en C/Java. Fue diseñado con el objetivo de ser fácil de leer, fácil de analizar y fácil de interpretar.

### Tipos de datos

| Tipo | Descripción | Ejemplo |
|------|-------------|---------|
| `int` | Número entero | `42`, `-3` |
| `float` | Número real | `3.14`, `-0.5` |
| `string` | Cadena de texto | `"hola mundo"` |
| `bool` | Booleano | `true`, `false` |

### Variables

La declaración es obligatoria antes del uso, el tipado es explícito y la inicialización es siempre requerida:

```
int x = 5;
float pi = 3.14;
string nombre = "MiniLang";
bool activo = true;
```

### Comentarios

Se soportan comentarios de línea con `//`:

```
// Esto es un comentario, es ignorado por el intérprete
int x = 10; // También al final de una línea
```

### Operadores

| Categoría | Operadores |
|-----------|------------|
| Aritméticos | `+`, `-`, `*`, `/` |
| Relacionales | `<`, `>`, `<=`, `>=` |
| Igualdad | `==`, `!=` |
| Lógicos | `&&`, `\|\|`, `!` |

El operador `+` también soporta concatenación de strings.

### Instrucción de salida

```
print("Hola mundo");
print(x);
print(x + 1);
```

### Condicional if-else

```
if (nota >= 6) {
    print("Aprobado");
} else {
    print("Desaprobado");
}
```

La rama `else` es opcional.

### repeat-until (variante diferencial)

```
repeat {
    print(n);
    n = n + 1;
} until (n == 5);
```

El bloque se ejecuta al menos una vez. El loop se detiene cuando la condición del `until` se evalúa como `true`.

---

## Decisiones de diseño

**Inicialización obligatoria**  
Se eligió requerir siempre un valor al declarar una variable. Esto elimina el estado indefinido y simplifica el análisis semántico: toda variable en la tabla de símbolos tiene un tipo y un valor conocido desde su creación.

**Tipado explícito estilo C**  
La sintaxis `tipo nombre = valor` fue preferida sobre alternativas como `var nombre : tipo` por ser más familiar para estudiantes que ya conocen Java, el lenguaje principal de la carrera.

**Operadores lógicos `&&` y `||`**  
Se optó por la notación simbólica en lugar de `and`/`or` para mantener consistencia con el estilo C/Java adoptado en el resto del lenguaje.

**`float` representado con `double` en Java**  
Internamente, los valores `float` de MiniLang se almacenan como `double` de Java para evitar pérdidas de precisión innecesarias durante la ejecución.

**Promoción automática int → float**  
Una variable `float` puede recibir un valor `int` sin error. El intérprete convierte automáticamente. Esto simplifica la escritura de programas sin perder seguridad de tipos.

**Condición del `until` debe ser `bool`**  
El analizador semántico verifica explícitamente que la expresión del `until` (y del `if`) sea de tipo `bool`. Cualquier otro tipo produce un error semántico con número de línea.

**Dos pasadas sobre el AST**  
Se separan el análisis semántico (`SemanticAnalyzer`) y la ejecución (`Interpreter`) en dos Visitors distintos. Esto garantiza que ningún programa con errores semánticos llegue a ejecutarse, y mantiene el código organizado y con responsabilidades claras.

---

## Estructura del proyecto

```
tp-interprete/
├── pom.xml
├── README.md
├── ejemplos/
│   ├── hola.ml
│   ├── condicional.ml
│   ├── repeat.ml
│   └── completo.ml
└── src/
    └── main/
        ├── antlr4/
        │   └── MiniLang.g4
        └── java/
            ├── Main.java
            ├── SymbolTable.java
            ├── SemanticException.java
            ├── SemanticAnalyzer.java
            └── Interpreter.java
```

---

## Instrucciones de compilación y ejecución

### Requisitos

- Java 17 o superior
- Maven 3.6 o superior

### Compilar

```bash
mvn compile
```

Este comando genera automáticamente las clases de ANTLR a partir de `MiniLang.g4` y compila todo el proyecto.

### Ejecutar un programa

```bash
mvn exec:java -Dexec.args="ejemplos/hola.ml"
```

O si preferís correrlo directamente con Java tras compilar:

```bash
java -cp target/classes:target/dependency/* Main ejemplos/hola.ml
```

### Ver errores

Si el programa tiene errores léxicos, sintácticos o semánticos, el intérprete los reporta con número de línea y se detiene sin ejecutar nada. Por ejemplo:

```
[Error semántico - línea 5] Variable 'x' no fue declarada.
```

---

## Ejemplos de uso

### hola.ml — Tipos y salida básica

```
// Programa básico de MiniLang
string nombre = "MiniLang";
int version = 1;

print("Hola desde ");
print(nombre);
print(version);
```

**Salida:**
```
Hola desde 
MiniLang
1
```

---

### condicional.ml — if-else

```
// Aprobado o desaprobado
int nota = 7;

if (nota >= 6) {
    print("Aprobado");
} else {
    print("Desaprobado");
}
```

**Salida:**
```
Aprobado
```

---

### repeat.ml — Variante diferencial repeat-until

```
// Cuenta del 1 al 5 con repeat-until
int n = 1;

repeat {
    print(n);
    n = n + 1;
} until (n > 5);
```

**Salida:**
```
1
2
3
4
5
```

---

### completo.ml — Todas las features combinadas

```
// Programa que combina tipos, condicionales y repeat-until
float precio = 100.0;
int descuento = 20;
bool aplicar = true;

if (aplicar) {
    precio = precio - descuento;
}

print("Precio final:");
print(precio);

// Mostrar precios con recargo creciente
int i = 1;
repeat {
    float conRecargo = precio + (precio * i);
    print(conRecargo);
    i = i + 1;
} until (i > 3);
```

**Salida:**
```
Precio final:
80.0
160.0
240.0
320.0
```

---

## Errores detectados

El intérprete detecta y reporta los siguientes errores:

| Tipo | Ejemplo que lo dispara |
|------|------------------------|
| Variable no declarada | `print(x);` sin declarar `x` |
| Redeclaración de variable | `int x = 1; int x = 2;` |
| Tipos incompatibles | `int x = "hola";` |
| Operación inválida | `int x = true + 1;` |
| División por cero | `int x = 5 / 0;` |
| Condición no booleana | `if (42) { ... }` |