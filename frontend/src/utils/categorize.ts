export type Category =
  | 'Income'
  | 'Rent'
  | 'Groceries'
  | 'Utilities'
  | 'Dining'
  | 'Shopping'
  | 'Transport'
  | 'Subscriptions'
  | 'Transfer'
  | 'Other';

export const CATEGORY_COLORS: Record<Category, string> = {
  Income: '#10b981',
  Rent: '#3b82f6',
  Groceries: '#f59e0b',
  Utilities: '#8b5cf6',
  Dining: '#ec4899',
  Shopping: '#06b6d4',
  Transport: '#14b8a6',
  Subscriptions: '#a855f7',
  Transfer: '#f97316',
  Other: '#94a3b8',
};

const RULES: { keywords: string[]; category: Category }[] = [
  { category: 'Income', keywords: ['salary', 'income', 'freelance', 'bonus', 'payroll', 'paycheck'] },
  { category: 'Rent', keywords: ['rent', 'landlord', 'lease', 'housing'] },
  { category: 'Groceries', keywords: ['grocery', 'market', 'supermart', 'supermarket', 'vegetables', 'kirana'] },
  { category: 'Utilities', keywords: ['electricity', 'water bill', 'gas bill', 'utility', 'broadband', 'internet bill', 'mobile bill'] },
  { category: 'Dining', keywords: ['restaurant', 'cafe', 'coffee', 'swiggy', 'zomato', 'food', 'pizza', 'burger'] },
  { category: 'Shopping', keywords: ['amazon', 'flipkart', 'shop', 'store', 'mall', 'clothing', 'myntra'] },
  { category: 'Transport', keywords: ['uber', 'ola', 'cab', 'taxi', 'fuel', 'petrol', 'diesel', 'metro', 'bus'] },
  { category: 'Subscriptions', keywords: ['netflix', 'spotify', 'subscription', 'prime', 'hotstar', 'youtube premium'] },
  { category: 'Transfer', keywords: ['transfer'] },
];

export function categorize(description: string | null | undefined): Category {
  if (!description) return 'Other';
  const lower = description.toLowerCase();
  for (const rule of RULES) {
    for (const kw of rule.keywords) {
      if (lower.includes(kw)) return rule.category;
    }
  }
  return 'Other';
}