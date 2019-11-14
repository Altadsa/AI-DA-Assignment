install.packages("rcompanion")
install.packages("e1071")
install.packages("gridExtra")
install.packages("pastecs")
library("pastecs")
library("rcompanion")
library(e1071)  
library("gridExtra")
library("ggpubr")
library(ggplot2)

#Get the Feature Data
#Change this to the directory of your features csv
filepath <- "C:\\Users\\spark\\Documents\\AI-DA-Assignment\\Assignment 2\\40178464_features.csv"
feature_col_names <- c("label", "index", "nr_pix", "height", "width", "span", "rows_wth_5", "cols_with_5", "neigh1", "neigh5", 
                       "left2tile", "right2tile", "verticalness", "top2tile", "bottom2tile", "horizontalness", "horizontal3tile", "vertical3tile", 
                       "nr_regions", "nr_eyes", "hollowness", "image_fill")

feature_data <- read.csv(filepath, header = TRUE, sep = "\t", col.names = feature_col_names)


#Get the Feature Data for Living Things
living_things_names <- c("Banana", "Cherry", "Flower", "Pear")
living_thing_data <- feature_data[which(feature_data$label %in% living_things_names), 1:22]


#Get the Feature Data for Nonliving Things
nonliving_things_names <- c("Envelope", "Golfclub", "Pencil", "Wineglass")
nonliving_thing_data <- feature_data[which(feature_data$label %in% nonliving_things_names), 1:22]

#1. Construct Histograms for nr_pix, height, cols_with_5 for different feature sets

one_to_three_histogram <- function(dataset)
{
  dataset_name <- deparse(substitute(dataset))
  hist(dataset$nr_pix, xlab = "Number of Pixels", main = sprintf("%s - No. of Pixels", dataset_name))
  hist(dataset$height, xlab = "Height", main = sprintf("%s - Height", dataset_name))
  hist(dataset$cols_with_5, xlab = "Columns with 5+", main = sprintf("%s - Columns with 5+", dataset_name))
}

one_to_three_histogram(living_thing_data)
one_to_three_histogram(nonliving_thing_data)
one_to_three_histogram(feature_data)

#2. Summary Statistics

#Function to create dataframe from datasets


summary_dataset <- function(featureset)
{
  summary_mean <- c()
  summary_sd <- c()
  summary_var <- c()
  for (i in 3:length(colnames(featureset)))
  {
    summary_mean[i] <- mean(featureset[[i]])
    summary_sd[i] <- sd(featureset[[i]])
    summary_var[i] <- var(featureset[[i]])
  }
  summary_statistics <- data.frame("Mean" = summary_mean, 
                                   "Sd" = summary_sd,
                                   "Var" = summary_var)
  print(length(rownames(summary_statistics)))
  rownames(summary_statistics) <- feature_col_names[1:22]

  return (summary_statistics)
}



living_summary <- summary_dataset(living_thing_data)
nonliving_summary <- summary_dataset(nonliving_thing_data)
all_summary <- summary_dataset(feature_data)

for (i in c(8,10,20,21))
{
  par(mfrow = c(1,2))
  plotNormalHistogram(living_thing_data[[i]],
                      main = sprintf("Living Things %s", 
                                     colnames(living_thing_data[i])),
                      xlab = colnames(living_thing_data[i]))
  plotNormalHistogram(nonliving_thing_data[[i]],
                      main = sprintf("Nonliving Things %s", 
                                     colnames(nonliving_thing_data[i])),
                      xlab = colnames(nonliving_thing_data[i]))
}


#3. Plot theoretical Normal Distribution for nr_pix

nr_pix <- feature_data$nr_pix
variance_nr_pix <- var(nr_pix)
mean_nr_pix <- mean(nr_pix)

#Histogram Only
hist(nr_pix, breaks = 10, density = 10,
     col = "red", xlab = "No. of Pixels", main = "Histogram of nr_pix")

#Normal Only
xfit <- seq(0, max(nr_pix), length = 250)
yfit <- dnorm(xfit, mean = mean_nr_pix, sd = sqrt(variance_nr_pix))

plot(xfit, yfit, 
     main = "Normal Distribution for nr_pix",
     xlab = "X",
     ylab = "Y")

#Normal over Histogram
hist_pix <- hist(nr_pix, breaks = 10, density = 10,
          col = "blue", xlab = "No. of Pixels", main = "nr_pix - normal over histogram")
yfit <- yfit * diff(hist_pix$mids[1:2]) * length(nr_pix)

lines(xfit, yfit, col = "black", lwd = 2)

#QQ Plot
qqnorm(nr_pix, main = "Q-Q Plot for nr_pix")
qqline(nr_pix)

#4. Cutoff value for nr_pix

plot(xfit, yfit, 
     main = "Normal Distribution for nr_pix",
     xlab = "nr_pix",
     ylab = "density")

rtail <- qnorm(p = 0.95, mean = mean_nr_pix, sd = sqrt(variance_nr_pix))
abline(v=rtail, col = "red", lty = 2, lwd = 3)

cutoff <- (rtail - mean_nr_pix) / sqrt(variance_nr_pix)


#5. Assessing the Normality of all the features

skew_index <- c()

for (i in 3:16)
{
  plot_feature <- feature_data[[i]]
  column_name <- colnames(feature_data[i])
  plot_skewness <- skewness(plot_feature)
  plot_mean <- mean(plot_feature)
  
  par(mfrow = c(2,1))
  feature_hist <- plotNormalHistogram(plot_feature, 
                      main = sprintf("Normal Histogram for %s", column_name),
                      xlab = column_name)
  abline(v=plot_mean, col = "red")
  mtext(sprintf("Skew %s", plot_skewness), side = 3)
  feature_qq <- qqnorm(plot_feature, 
                       main = sprintf("Q-Q Plot for %s", column_name))
  feature_qq <- qqline(plot_feature)


  if (abs(plot_skewness) >= 1)
  {
    skew_index <- c(skew_index, i)
  }
}

for (i in skew_index)
{
  main <- colnames(feature_data[i])
  transformation <- "Log Base-10"
  transform_data <- log10(feature_data[[i]])
  if (!is.finite(min(transform_data)))
  {
    transformation <- "Sqrt"
    transform_data <- sqrt(feature_data[[i]])
  }
  plotNormalHistogram(transform_data,
                      main = sprintf("%s Transformation on %s", transformation, main))
  mtext(sprintf("Skew %s", skewness(transform_data)), side = 3)
  qqnorm(transform_data,
         main = sprintf("%s Q-Q Plot for %s", transformation, main))
  qqline(transform_data)
  
}

#6. Determining Linear Association between height and span

height <- feature_data$height
span <- feature_data$span

plot(height, span, main = "Height against Span",
     xlab = "Height",
     ylab = "Span")
abline(lm(span~height), col = "red")

cor.test(height, span, method = "pearson")

#7. Determine if nr_pix is a useful discriminating feature for non living things

#Box plot of different groups
ggboxplot(nonliving_thing_data, x = "label", y = "nr_pix",
          color = "label", palette = c("#0000FF", "#00FF00", "#FF0000", "#FF00FF"),
          ylab = "Pixel Count", xlab = "Object")
#Generate the Statistics
nonliving_means <- c()
nonliving_sd <- c()
nonliving_length <-c()
for (i in 1:length(nonliving_things_names))
{
  thing_name <- nonliving_things_names[i]
  thing_pix <- nonliving_thing_data[which(nonliving_thing_data$label %in% thing_name), 3]
  
  nonliving_means[i] <- mean(thing_pix)
  nonliving_sd[i] <- sd(thing_pix)
  nonliving_length[i] <- length(thing_pix)
}

nonliving_stats <- data.frame("n" = nonliving_length, 
                              "mean" = nonliving_means, 
                              "sd" = nonliving_sd, 
                              row.names = nonliving_things_names)

#Calculate Degress of freedom
df1 <- length(rownames(nonliving_stats)) - 1
df2 <- (sum(nonliving_stats$n) - 1) - df1

#Calculate Variability between groups
var_bet <- c()
pix_mean <- mean(nonliving_thing_data$nr_pix)
for (i in 1:length(rownames(nonliving_stats)))
{
  thing <- nonliving_stats[i,]
  var_bet[i] <- (thing[1] * (thing[2] - pix_mean)^2)
}

nonliving_stats$var <- var_bet

#calculate Total Variability between all things
var_tot <- c()
for (i in 1:length(nonliving_thing_data$nr_pix))
{
  nr_pixels <- nonliving_thing_data[i,3]
  var_tot[i] <- ((nr_pixels - pix_mean)^2)
}

#Calculate Means and F value
msg <- Reduce("+", nonliving_stats$var) / df1
mse <- (Reduce("+", var_tot) - Reduce("+", nonliving_stats$var)) / df2
f_val <- msg / mse

pF <- 1 -pf(f_val, df1, df2)

#Summarise the test using the aov function to display the table of results
pix_aov <- aov(nr_pix ~ label, data = nonliving_thing_data)

summary(pix_aov)
pf(414.2, df1 = 3, df2 = 76)

#8. Determine if hollowness is useful for discriminating between nonliving things

#Take the hollowness from nonliving things, randomly assign them to groups


nr_randomizations <- 10000
anova_results <- rep(0, nr_randomizations)
for (ii in 1:1)
{
  start_group <- 1
  end_group <- 20
  group_size <- 20
  sample_data <- data.frame("label" = "null", "hollowness" = sample(nonliving_thing_data$hollowness), stringsAsFactors = FALSE)
  for (thing in nonliving_things_names)
  {
    sample_data$label[start_group:end_group] <- toString(thing)
    start_group <- start_group + group_size
    end_group <- end_group + group_size
  }
  sample_aov <- aov(hollowness ~ label, data = sample_data)
  anova_results[ii] <- summary(sample_aov)[[1]]$F[1]
  
}

plotNormalHistogram(anova_results,
                    main = "Normal Histogram of random F statisitics",
                    xlab = "F-value")
hollowness_aov <- aov(hollowness ~ label, data = nonliving_thing_data)
actual_f <- summary(hollowness_aov)[[1]]$F[1]
pf(mean(anova_results), df1 = 3, df2 = 76)
plotNormalHistogram(nonliving_thing_data$hollowness,
                    main = "Normal Histogram for hollowness of nonliving things",
                    xlab = "hollowness")

result <- pairwise.t.test(nonliving_thing_data$hollowness, nonliving_thing_data$label, data=nonliving_thing_data,
                          p.adjust.method = "bonferroni")
result$p.value

#9.

test_table <- data.frame("feature" = colnames(feature_data)[3:22], "t_value" = 0)
for (i in 3:22)
{
  t_test <- t.test(living_thing_data[[i]], nonliving_thing_data[[i]])
  rbind(test_table, "feature" = colnames(living_thing_data[i]), "t_value" = t_test$statistic)
}

t <- t.test(living_thing_data$hollowness, nonliving_thing_data$hollowness)

#10.

f_table <- data.frame("feature" = colnames(nonliving_thing_data)[4:8], "F_value" = 0)
f_values <- c()
highest_f <- 0
highest_index <- -1
for (i in 4:8)
{
  feature <- nonliving_thing_data[[i]]
  feature_aov <- aov(feature ~ label, data = nonliving_thing_data)
  
  f_val <- summary(feature_aov)[[1]]$F[1]
  
  if (f_val > highest_f) {
    highest_index <- i
    highest_f <- f_val
  }
  f_values <- c(f_values, f_val)
}

f_table$F_value <- f_values

feature_name <- nonliving_thing_data[[highest_index]]
summary(aov(feature_name ~ label, data = nonliving_thing_data))
p_value <- 1 - pf(highest_f, 3, 76)

pair_t_results <- pairwise.t.test(nonliving_thing_data[[highest_index]], nonliving_thing_data$label, data = nonliving_thing_data,
                p.adjust.method = "bonferroni")
pair_t_results
